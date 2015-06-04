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
package fm.liu.timo.net.mysql;

import java.nio.ByteBuffer;

import fm.liu.timo.mysql.MySQLMessage;

/**
 * From client to server whenever the client wants the server to do something.
 * 
 * <pre>
 * Bytes         Name
 * -----         ----
 * 1             command
 * n             arg
 * 
 * command:      The most common value is 03 COM_QUERY, because
 *               INSERT UPDATE DELETE SELECT etc. have this code.
 *               The possible values at time of writing (taken
 *               from /include/mysql_com.h for enum_server_command) are:
 * 
 *               #      Name                Associated client function
 *               -      ----                --------------------------
 *               0x00   COM_SLEEP           (none, this is an internal thread state)
 *               0x01   COM_QUIT            mysql_close
 *               0x02   COM_INIT_DB         mysql_select_db 
 *               0x03   COM_QUERY           mysql_real_query
 *               0x04   COM_FIELD_LIST      mysql_list_fields
 *               0x05   COM_CREATE_DB       mysql_create_db (deprecated)
 *               0x06   COM_DROP_DB         mysql_drop_db (deprecated)
 *               0x07   COM_REFRESH         mysql_refresh
 *               0x08   COM_SHUTDOWN        mysql_shutdown
 *               0x09   COM_STATISTICS      mysql_stat
 *               0x0a   COM_PROCESS_INFO    mysql_list_processes
 *               0x0b   COM_CONNECT         (none, this is an internal thread state)
 *               0x0c   COM_PROCESS_KILL    mysql_kill
 *               0x0d   COM_DEBUG           mysql_dump_debug_info
 *               0x0e   COM_PING            mysql_ping
 *               0x0f   COM_TIME            (none, this is an internal thread state)
 *               0x10   COM_DELAYED_INSERT  (none, this is an internal thread state)
 *               0x11   COM_CHANGE_USER     mysql_change_user
 *               0x12   COM_BINLOG_DUMP     sent by the slave IO thread to request a binlog
 *               0x13   COM_TABLE_DUMP      LOAD TABLE ... FROM MASTER (deprecated)
 *               0x14   COM_CONNECT_OUT     (none, this is an internal thread state)
 *               0x15   COM_REGISTER_SLAVE  sent by the slave to register with the master (optional)
 *               0x16   COM_STMT_PREPARE    mysql_stmt_prepare
 *               0x17   COM_STMT_EXECUTE    mysql_stmt_execute
 *               0x18   COM_STMT_SEND_LONG_DATA mysql_stmt_send_long_data
 *               0x19   COM_STMT_CLOSE      mysql_stmt_close
 *               0x1a   COM_STMT_RESET      mysql_stmt_reset
 *               0x1b   COM_SET_OPTION      mysql_set_server_option
 *               0x1c   COM_STMT_FETCH      mysql_stmt_fetch
 * 
 * arg:          The text of the command is just the way the user typed it, there is no processing
 *               by the client (except removal of the final ';').
 *               This field is not a null-terminated string; however,
 *               the size can be calculated from the packet size,
 *               and the MySQL client appends '\0' when receiving.
 *               
 * @see http://forge.mysql.com/wiki/MySQL_Internals_ClientServer_Protocol#Command_Packet_.28Overview.29
 * </pre>
 * 
 */
public class CommandPacket extends MySQLClientPacket {

	public byte command;
	public byte[] arg;
	

	@SuppressWarnings("unused")
	private CommandPacket() {
	}

	public CommandPacket(byte commandType) {		
		this.command = commandType;
		this.packetId = 0;
	}

	/**
	 * none, this is an internal thread state
	 */
	public static final byte COM_SLEEP = 0;

	/**
	 * mysql_close
	 */
	public static final byte COM_QUIT = 1;

	/**
	 * mysql_select_db
	 */
	public static final byte COM_INIT_DB = 2;

	/**
	 * mysql_real_query
	 */
	public static final byte COM_QUERY = 3;

	/**
	 * mysql_list_fields
	 */
	public static final byte COM_FIELD_LIST = 4;

	/**
	 * mysql_create_db (deprecated)
	 */
	public static final byte COM_CREATE_DB = 5;

	/**
	 * mysql_drop_db (deprecated)
	 */
	public static final byte COM_DROP_DB = 6;

	/**
	 * mysql_refresh
	 */
	public static final byte COM_REFRESH = 7;

	/**
	 * mysql_shutdown
	 */
	public static final byte COM_SHUTDOWN = 8;

	/**
	 * mysql_stat
	 */
	public static final byte COM_STATISTICS = 9;

	/**
	 * mysql_list_processes
	 */
	public static final byte COM_PROCESS_INFO = 10;

	/**
	 * none, this is an internal thread state
	 */
	public static final byte COM_CONNECT = 11;

	/**
	 * mysql_kill
	 */
	public static final byte COM_PROCESS_KILL = 12;

	/**
	 * mysql_dump_debug_info
	 */
	public static final byte COM_DEBUG = 13;

	/**
	 * mysql_ping
	 */
	public static final byte COM_PING = 14;

	/**
	 * none, this is an internal thread state
	 */
	public static final byte COM_TIME = 15;

	/**
	 * none, this is an internal thread state
	 */
	public static final byte COM_DELAYED_INSERT = 16;

	/**
	 * mysql_change_user
	 */
	public static final byte COM_CHANGE_USER = 17;

	/**
	 * used by slave server mysqlbinlog
	 */
	public static final byte COM_BINLOG_DUMP = 18;

	/**
	 * used by slave server to get master table
	 */
	public static final byte COM_TABLE_DUMP = 19;

	/**
	 * used by slave to log connection to master
	 */
	public static final byte COM_CONNECT_OUT = 20;

	/**
	 * used by slave to register to master
	 */
	public static final byte COM_REGISTER_SLAVE = 21;

	/**
	 * mysql_stmt_prepare
	 */
	public static final byte COM_STMT_PREPARE = 22;

	/**
	 * mysql_stmt_execute
	 */
	public static final byte COM_STMT_EXECUTE = 23;

	/**
	 * mysql_stmt_send_long_data
	 */
	public static final byte COM_STMT_SEND_LONG_DATA = 24;

	/**
	 * mysql_stmt_close
	 */
	public static final byte COM_STMT_CLOSE = 25;

	/**
	 * mysql_stmt_reset
	 */
	public static final byte COM_STMT_RESET = 26;

	/**
	 * mysql_set_server_option
	 */
	public static final byte COM_SET_OPTION = 27;

	/**
	 * mysql_stmt_fetch
	 */
	public static final byte COM_STMT_FETCH = 28;

	/**
	 * Timo heartbeat
	 */
	public static final byte COM_HEARTBEAT = 64;
	
	public static final byte[] PING = new byte[] { 1, 0, 0, 0, 14 };
	public static final byte[] QUIT = new byte[] { 1, 0, 0, 0, 1 };
	
	@Override
	protected void readBody(MySQLMessage mm){
		command = mm.read();
		arg = mm.readBytes();
	}

	@Override
	public int calcPacketSize() {
		return 1 + arg.length;
	}

	@Override
	protected String getPacketInfo() {
		return "MySQL Command Packet";
	}

	@Override
	protected void writeBody(ByteBuffer buffer) {
		buffer.put(command);
		buffer.put(arg);		
	}

}