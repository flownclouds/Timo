/*
 * Copyright 2015 Liu Huanting.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package fm.liu.timo.mysql.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import fm.liu.timo.config.Fields;
import fm.liu.timo.mysql.BindValue;
import fm.liu.timo.mysql.BindValueUtil;
import fm.liu.timo.mysql.MySQLMessage;
import fm.liu.timo.mysql.PreparedStatement;

/**
 * <pre>
 *  Bytes                      Name
 *  -----                      ----
 *  1                          code
 *  4                          statement_id
 *  1                          flags
 *  4                          iteration_count 
 *  (param_count+7)/8          null_bit_map
 *  1                          new_parameter_bound_flag (if new_params_bound == 1:)
 *  n*2                        type of parameters
 *  n                          values for the parameters   
 *  --------------------------------------------------------------------------------
 *  code:                      always COM_EXECUTE
 *  
 *  statement_id:              statement identifier
 *  
 *  flags:                     reserved for future use. In MySQL 4.0, always 0.
 *                             In MySQL 5.0: 
 *                               0: CURSOR_TYPE_NO_CURSOR
 *                               1: CURSOR_TYPE_READ_ONLY
 *                               2: CURSOR_TYPE_FOR_UPDATE
 *                               4: CURSOR_TYPE_SCROLLABLE
 *  
 *  iteration_count:           reserved for future use. Currently always 1.
 *  
 *  null_bit_map:              A bitmap indicating parameters that are NULL.
 *                             Bits are counted from LSB, using as many bytes
 *                             as necessary ((param_count+7)/8)
 *                             i.e. if the first parameter (parameter 0) is NULL, then
 *                             the least significant bit in the first byte will be 1.
 *  
 *  new_parameter_bound_flag:  Contains 1 if this is the first time
 *                             that "execute" has been called, or if
 *                             the parameters have been rebound.
 *  
 *  type:                      Occurs once for each parameter; 
 *                             The highest significant bit of this 16-bit value
 *                             encodes the unsigned property. The other 15 bits
 *                             are reserved for the type (only 8 currently used).
 *                             This block is sent when parameters have been rebound
 *                             or when a prepared statement is executed for the 
 *                             first time.
 * 
 *  values:                    for all non-NULL values, each parameters appends its value
 *                             as described in Row Data Packet: Binary (column values)
 * &#64;see http://dev.mysql.com/doc/internals/en/execute-packet.html
 * </pre>
 * 
 */
public class ExecutePacket extends MySQLPacket {

    public byte                 code;
    public long                 statementId;
    public byte                 flags;
    public long                 iterationCount;
    public byte[]               nullBitMap;
    public byte                 newParameterBoundFlag;
    public BindValue[]          values;
    protected PreparedStatement pstmt;

    public ExecutePacket(PreparedStatement pstmt) {
        this.pstmt = pstmt;
        this.values = new BindValue[pstmt.getParametersNumber()];
    }

    public void read(byte[] data, String charset) throws UnsupportedEncodingException {
        MySQLMessage mm = new MySQLMessage(data);
        packetLength = mm.readUB3();
        packetId = mm.read();
        code = mm.read();
        statementId = mm.readUB4();
        flags = mm.read();
        iterationCount = mm.readUB4();

        // 读取NULL指示器数据
        int parameterCount = values.length;
        nullBitMap = new byte[(parameterCount + 7) / 8];
        for (int i = 0; i < nullBitMap.length; i++) {
            nullBitMap[i] = mm.read();
        }

        // 当newParameterBoundFlag==1时，更新参数类型。
        newParameterBoundFlag = mm.read();
        if (newParameterBoundFlag == (byte) 1) {
            for (int i = 0; i < parameterCount; i++) {
                pstmt.getParametersType()[i] = mm.readUB2();
            }
        }

        // 设置参数类型和读取参数值
        byte[] nullBitMap = this.nullBitMap;
        for (int i = 0; i < parameterCount; i++) {
            BindValue bv = new BindValue();
            bv.type = pstmt.getParametersType()[i];
            if ((nullBitMap[i / 8] & (1 << (i & 7))) != 0) {
                bv.isNull = true;
            } else {
                BindValueUtil.read(mm, bv, charset);
            }
            values[i] = bv;
        }
    }

    public String getSQL() {
        StringBuilder sb = new StringBuilder();
        String[] stmts = pstmt.getStatements();
        int length = stmts.length;
        if (pstmt.isEndsWithQuestionMark()) {
            for (int i = 0; i < length; i++) {
                sb.append(stmts[i]).append(getValue(values[i]));
            }
        } else {
            for (int i = 0; i < length - 1; i++) {
                sb.append(stmts[i]).append(getValue(values[i]));
            }
            sb.append(stmts[length - 1]);
        }
        return sb.toString();
    }

    private Object getValue(BindValue bv) {
        if (bv.isNull) {
            return "NULL";
        } else {
            switch (bv.type & 0xff) {
                case Fields.FIELD_TYPE_BIT:
                    return bv.value;
                case Fields.FIELD_TYPE_TINY:
                    return bv.byteBinding;
                case Fields.FIELD_TYPE_SHORT:
                    return bv.shortBinding;
                case Fields.FIELD_TYPE_LONG:
                    return bv.intBinding;
                case Fields.FIELD_TYPE_LONGLONG:
                    return bv.longBinding;
                case Fields.FIELD_TYPE_FLOAT:
                    return bv.floatBinding;
                case Fields.FIELD_TYPE_DOUBLE:
                    return bv.doubleBinding;
                case Fields.FIELD_TYPE_TIME:
                case Fields.FIELD_TYPE_DATE:
                case Fields.FIELD_TYPE_DATETIME:
                case Fields.FIELD_TYPE_TIMESTAMP:
                case Fields.FIELD_TYPE_VAR_STRING:
                case Fields.FIELD_TYPE_STRING:
                case Fields.FIELD_TYPE_VARCHAR:
                case Fields.FIELD_TYPE_DECIMAL:
                case Fields.FIELD_TYPE_NEW_DECIMAL:
                    if (bv.value == null) {
                        return "NULL";
                    }
                    return "'" + bv.value + "'";
                default:
                    throw new IllegalArgumentException(
                            "bindValue error,unsupported type:" + bv.type);
            }
        }
    }

    @Override
    public int calcPacketSize() {
        throw new RuntimeException("calcPacketSize for ExecutePacket not implement!");
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL Execute Packet";
    }

    @Override
    protected void writeBody(ByteBuffer buffer) {
        throw new RuntimeException("writeBody for ExecutePacket not implement!");
    }

    @Override
    protected void readBody(MySQLMessage mm) {
        throw new RuntimeException("readBody for ExecutePacket not implement!");
    }

}
