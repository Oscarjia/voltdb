/* Copyright (c) 2001-2011, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb_voltpatches.dbinfo;

import java.lang.reflect.Constructor;

import org.hsqldb_voltpatches.Database;
import org.hsqldb_voltpatches.Session;
import org.hsqldb_voltpatches.Table;
import org.hsqldb_voltpatches.lib.IntValueHashMap;
import org.hsqldb_voltpatches.persist.PersistentStore;

// fredt@users - 1.7.2 - structural modifications to allow inheritance
// boucherB@users 20020305 - completed inheritance work, including final access
// boucherB@users 20020305 - javadoc updates/corrections
// boucherB@users 20020305 - SYSTEM_VIEWS brought in line with SQL 200n
// boucherb@users 20050514 - further SQL 200n metdata support

/**
 * Base class for system tables. Includes a factory method which returns the
 * most complete implementation available in the jar. This base implementation
 * knows the names of all system tables but returns null for any system table.
 * <p>
 * This class has been developed from scratch to replace the previous
 * DatabaseInformation implementations. <p>
 *
 * @author Campbell Boucher-Burnet (boucherb@users dot sourceforge.net)
 * @version 2.0.1
 * @since 1.7.2
 */
public class DatabaseInformation {

    // ids for system table names strictly in order of sysTableNames[]
    protected static final int SYSTEM_BESTROWIDENTIFIER = 0;
    protected static final int SYSTEM_COLUMNS           = 1;
    protected static final int SYSTEM_CROSSREFERENCE    = 2;
    protected static final int SYSTEM_INDEXINFO         = 3;
    protected static final int SYSTEM_PRIMARYKEYS       = 4;
    protected static final int SYSTEM_PROCEDURECOLUMNS  = 5;
    protected static final int SYSTEM_PROCEDURES        = 6;
    protected static final int SYSTEM_SCHEMAS           = 7;
    protected static final int SYSTEM_TABLES            = 8;
    protected static final int SYSTEM_TABLETYPES        = 9;
    protected static final int SYSTEM_TYPEINFO          = 10;
    protected static final int SYSTEM_UDTS              = 11;
    protected static final int SYSTEM_USERS             = 12;    //-- ref in SqlFile only
    protected static final int SYSTEM_VERSIONCOLUMNS    = 13;    //-- returns autogenerated columns
    protected static final int SYSTEM_SEQUENCES = 14;            //-- same as SEQUENCES

    // HSQLDB-specific
    protected static final int SYSTEM_CACHEINFO             = 15;
    protected static final int SYSTEM_COLUMN_SEQUENCE_USAGE = 16;
    protected static final int SYSTEM_COMMENTS              = 17;
    protected static final int SYSTEM_CONNECTION_PROPERTIES = 18;
    protected static final int SYSTEM_PROPERTIES            = 19;
    protected static final int SYSTEM_SESSIONINFO           = 20;
    protected static final int SYSTEM_SESSIONS              = 21;
    protected static final int SYSTEM_TEXTTABLES            = 22;
    protected static final int SYSTEM_TABLESTATS            = 23;

    // SQL 200n tables
    protected static final int ADMINISTRABLE_ROLE_AUTHORIZATIONS = 24;
    protected static final int APPLICABLE_ROLES                  = 25;
    protected static final int ASSERTIONS                        = 26;
    protected static final int AUTHORIZATIONS                    = 27;
    protected static final int CHARACTER_SETS                    = 28;
    protected static final int CHECK_CONSTRAINT_ROUTINE_USAGE    = 29;
    protected static final int CHECK_CONSTRAINTS                 = 30;
    protected static final int COLLATIONS                        = 31;
    protected static final int COLUMN_COLUMN_USAGE               = 32;
    protected static final int COLUMN_DOMAIN_USAGE               = 33;
    protected static final int COLUMN_PRIVILEGES                 = 34;
    protected static final int COLUMN_UDT_USAGE                  = 35;
    protected static final int COLUMNS                           = 36;
    protected static final int CONSTRAINT_COLUMN_USAGE           = 37;
    protected static final int CONSTRAINT_TABLE_USAGE            = 38;
    protected static final int DATA_TYPE_PRIVILEGES              = 39;
    protected static final int DOMAIN_CONSTRAINTS                = 40;
    protected static final int DOMAINS                           = 41;
    protected static final int ELEMENT_TYPES                     = 42;
    protected static final int ENABLED_ROLES                     = 43;
    protected static final int INFORMATION_SCHEMA_CATALOG_NAME   = 44;
    protected static final int JAR_JAR_USAGE                     = 45;
    protected static final int JARS                              = 46;
    protected static final int KEY_COLUMN_USAGE                  = 47;
    protected static final int METHOD_SPECIFICATIONS             = 48;
    protected static final int MODULE_COLUMN_USAGE               = 49;
    protected static final int MODULE_PRIVILEGES                 = 50;
    protected static final int MODULE_TABLE_USAGE                = 51;
    protected static final int MODULES                           = 52;
    protected static final int PARAMETERS                        = 53;
    protected static final int REFERENTIAL_CONSTRAINTS           = 54;
    protected static final int ROLE_AUTHORIZATION_DESCRIPTORS    = 55;
    protected static final int ROLE_COLUMN_GRANTS                = 56;
    protected static final int ROLE_MODULE_GRANTS                = 57;
    protected static final int ROLE_ROUTINE_GRANTS               = 58;
    protected static final int ROLE_TABLE_GRANTS                 = 59;
    protected static final int ROLE_UDT_GRANTS                   = 60;
    protected static final int ROLE_USAGE_GRANTS                 = 61;
    protected static final int ROUTINE_COLUMN_USAGE              = 62;
    protected static final int ROUTINE_JAR_USAGE                 = 63;
    protected static final int ROUTINE_PRIVILEGES                = 64;
    protected static final int ROUTINE_ROUTINE_USAGE             = 65;
    protected static final int ROUTINE_SEQUENCE_USAGE            = 66;
    protected static final int ROUTINE_TABLE_USAGE               = 67;
    protected static final int ROUTINES                          = 68;
    protected static final int SCHEMATA                          = 69;
    protected static final int SEQUENCES                         = 70;
    protected static final int SQL_FEATURES                      = 71;
    protected static final int SQL_IMPLEMENTATION_INFO           = 72;
    protected static final int SQL_PACKAGES                      = 73;
    protected static final int SQL_PARTS                         = 74;
    protected static final int SQL_SIZING                        = 75;
    protected static final int SQL_SIZING_PROFILES               = 76;
    protected static final int TABLE_CONSTRAINTS                 = 77;
    protected static final int TABLE_PRIVILEGES                  = 78;
    protected static final int TABLES                            = 79;
    protected static final int TRANSLATIONS                      = 80;
    protected static final int TRIGGER_COLUMN_USAGE              = 81;
    protected static final int TRIGGER_ROUTINE_USAGE             = 82;
    protected static final int TRIGGER_SEQUENCE_USAGE            = 83;
    protected static final int TRIGGER_TABLE_USAGE               = 84;
    protected static final int TRIGGERED_UPDATE_COLUMNS          = 85;
    protected static final int TRIGGERS                          = 86;
    protected static final int TYPE_JAR_USAGE                    = 87;
    protected static final int UDT_PRIVILEGES                    = 88;
    protected static final int USAGE_PRIVILEGES                  = 89;
    protected static final int USER_DEFINED_TYPES                = 90;
    protected static final int VIEW_COLUMN_USAGE                 = 91;
    protected static final int VIEW_ROUTINE_USAGE                = 92;
    protected static final int VIEW_TABLE_USAGE                  = 93;
    protected static final int VIEWS                             = 94;

    /** system table names strictly in order of their ids */
    protected static final String[] sysTableNames = {
        "SYSTEM_BESTROWIDENTIFIER",                              //
        "SYSTEM_COLUMNS",                                        //
        "SYSTEM_CROSSREFERENCE",                                 //
        "SYSTEM_INDEXINFO",                                      //
        "SYSTEM_PRIMARYKEYS",                                    //
        "SYSTEM_PROCEDURECOLUMNS",                               //
        "SYSTEM_PROCEDURES",                                     //
        "SYSTEM_SCHEMAS",                                        //
        "SYSTEM_TABLES",                                         //
        "SYSTEM_TABLETYPES",                                     //
        "SYSTEM_TYPEINFO",                                       //
        "SYSTEM_UDTS",                                           //
        "SYSTEM_USERS",                                          //
        "SYSTEM_VERSIONCOLUMNS",                                 //
        "SYSTEM_SEQUENCES",                                      //

        // HSQLDB-specific
        "SYSTEM_CACHEINFO",                                      //
        "SYSTEM_COLUMN_SEQUENCE_USAGE",                          //
        "SYSTEM_COMMENTS",                                       //
        "SYSTEM_CONNECTION_PROPERTIES",                          //
        "SYSTEM_PROPERTIES",                                     //
        "SYSTEM_SESSIONINFO",                                    //
        "SYSTEM_SESSIONS",                                       //
        "SYSTEM_TEXTTABLES",                                     //
        "SYSTEM_TABLESTATS",                                     //

        // SQL 200n
        "ADMINISTRABLE_ROLE_AUTHORIZATIONS",                     //
        "APPLICABLE_ROLES",                                      //
        "ASSERTIONS",                                            //
        "AUTHORIZATIONS",                                        //
        "CHARACTER_SETS",                                        //
        "CHECK_CONSTRAINT_ROUTINE_USAGE",                        //
        "CHECK_CONSTRAINTS",                                     //
        "COLLATIONS",                                            //
        "COLUMN_COLUMN_USAGE",                                   //
        "COLUMN_DOMAIN_USAGE",                                   //
        "COLUMN_PRIVILEGES",                                     //
        "COLUMN_UDT_USAGE",                                      //
        "COLUMNS",                                               //
        "CONSTRAINT_COLUMN_USAGE",                               //
        "CONSTRAINT_TABLE_USAGE",                                //
        "DATA_TYPE_PRIVILEGES",                                  //
        "DOMAIN_CONSTRAINTS",                                    //
        "DOMAINS",                                               //
        "ELEMENT_TYPES",                                         //
        "ENABLED_ROLES",                                         //
        "INFORMATION_SCHEMA_CATALOG_NAME",                       //
        "JAR_JAR_USAGE",                                         //
        "JARS",                                                  //
        "KEY_COLUMN_USAGE",                                      //
        "METHOD_SPECIFICATIONS",                                 //
        "MODULE_COLUMN_USAGE",                                   //
        "MODULE_PRIVILEGES",                                     //
        "MODULE_TABLE_USAGE",                                    //
        "MODULES",                                               //
        "PARAMETERS",                                            //
        "REFERENTIAL_CONSTRAINTS",                               //
        "ROLE_AUTHORIZATION_DESCRIPTORS",                        //
        "ROLE_COLUMN_GRANTS",                                    //
        "ROLE_MODULE_GRANTS",                                    //
        "ROLE_ROUTINE_GRANTS",                                   //
        "ROLE_TABLE_GRANTS",                                     //
        "ROLE_UDT_GRANTS",                                       //
        "ROLE_USAGE_GRANTS",                                     //
        "ROUTINE_COLUMN_USAGE",                                  //
        "ROUTINE_JAR_USAGE",                                     //
        "ROUTINE_PRIVILEGES",                                    //
        "ROUTINE_ROUTINE_USAGE",                                 //
        "ROUTINE_SEQUENCE_USAGE",                                //
        "ROUTINE_TABLE_USAGE",                                   //
        "ROUTINES",                                              //
        "SCHEMATA",                                              //
        "SEQUENCES",                                             //
        "SQL_FEATURES",                                          //
        "SQL_IMPLEMENTATION_INFO",                               //
        "SQL_PACKAGES",                                          //
        "SQL_PARTS",                                             //
        "SQL_SIZING",                                            //
        "SQL_SIZING_PROFILES",                                   //
        "TABLE_CONSTRAINTS",                                     //
        "TABLE_PRIVILEGES",                                      //
        "TABLES",                                                //
        "TRANSLATIONS",                                          //
        "TRIGGER_COLUMN_USAGE",                                  //
        "TRIGGER_ROUTINE_USAGE",                                 //
        "TRIGGER_SEQUENCE_USAGE",                                //
        "TRIGGER_TABLE_USAGE",                                   //
        "TRIGGERED_UPDATE_COLUMNS",                              //
        "TRIGGERS",                                              //
        "TYPE_JAR_USAGE",                                        //
        "UDT_PRIVILEGES",                                        //
        "USAGE_PRIVILEGES",                                      //
        "USER_DEFINED_TYPES",                                    //
        "VIEW_COLUMN_USAGE",                                     //
        "VIEW_ROUTINE_USAGE",                                    //
        "VIEW_TABLE_USAGE",                                      //
        "VIEWS",                                                 //
    };

    /** Map: table name => table id */
    protected static final IntValueHashMap sysTableNamesMap;

    static {
        synchronized (DatabaseInformation.class) {
            sysTableNamesMap = new IntValueHashMap(97);

            for (int i = 0; i < sysTableNames.length; i++) {
                sysTableNamesMap.put(sysTableNames[i], i);
            }
        }
    }

    static int getSysTableID(String token) {
        return sysTableNamesMap.get(token, -1);
    }

    /** Database for which to produce tables */
    protected final Database database;

    /**
     * state flag -- if true, contentful tables are to be produced, else
     * empty (surrogate) tables are to be produced.  This allows faster
     * database startup where user views reference system tables and faster
     * system table structural reflection for table metadata.
     */
    protected boolean withContent = false;

    /**
     * Factory method returns the fullest system table producer
     * implementation available.  This instantiates implementations beginning
     * with the most complete, finally choosing an empty table producer
     * implemenation (this class) if no better instance can be constructed.
     * @param db The Database object for which to produce system tables
     * @return the fullest system table producer
     *      implementation available
     */
    public static final DatabaseInformation newDatabaseInformation(
            Database db) {

        Class c = null;

        try {
            c = Class.forName("org.hsqldb_voltpatches.dbinfo.DatabaseInformationFull");
        } catch (Exception e) {
            try {
                c = Class.forName("org.hsqldb_voltpatches.dbinfo.DatabaseInformationMain");
            } catch (Exception e2) {
                c = DatabaseInformation.class;
            }
        }

        try {
            Class[]     ctorParmTypes = new Class[]{ Database.class };
            Object[]    ctorParms     = new Object[]{ db };
            Constructor ctor = c.getDeclaredConstructor(ctorParmTypes);

            return (DatabaseInformation) ctor.newInstance(ctorParms);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new DatabaseInformation(db);
    }

    /**
     * Constructs a new DatabaseInformation instance which knows the names of
     * all system tables (isSystemTable()) but simpy returns null for all
     * getSystemTable() requests. <p>
     *
     * @param db The Database object for which to produce system tables
     */
    DatabaseInformation(Database db) {
        database = db;
    }

    /**
     * Tests if the specified name is that of a system table. <p>
     *
     * @param name the name to test
     * @return true if the specified name is that of a system table
     */
    final boolean isSystemTable(String name) {
        return sysTableNamesMap.containsKey(name);
    }

    /**
     * Retrieves a table with the specified name whose content may depend on
     * the execution context indicated by the session argument as well as the
     * current value of <code>withContent</code>. <p>
     *
     * @param session the context in which to produce the table
     * @param name the name of the table to produce
     * @return a table corresponding to the name and session arguments, or
     *      <code>null</code> if there is no such table to be produced
     */
    public Table getSystemTable(Session session, String name) {
        return null;
    }

    /**
     * Sets the store for the given session, populates the store if necessary.
     */
    public void setStore(Session session, Table table,
                                      PersistentStore store) {}

    /**
     * Switches this table producer between producing empty (surrogate)
     * or contentful tables. <p>
     *
     * @param withContent if true, then produce contentful tables, else
     *        produce emtpy (surrogate) tables
     */
    public final void setWithContent(boolean withContent) {
        this.withContent = withContent;
    }
}
