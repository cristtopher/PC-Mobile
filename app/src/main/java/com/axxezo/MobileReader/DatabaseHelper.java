package com.axxezo.MobileReader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by axxezo on 14/11/2016.
 */

public class DatabaseHelper extends SQLiteOpenHelper {


    //context
    private Context context;
    private static DatabaseHelper sInstance;
    private SQLiteDatabase db;

    //create a unique instance of DB

    public static synchronized DatabaseHelper getInstance(Context context) {
        //one single instance of DB
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    //Table names
    private static final String TABLE_PEOPLE = "PEOPLE";
    private static final String TABLE_RECORDS = "RECORDS";
    private static final String TABLE_ROUTES = "ROUTES";
    private static final String TABLE_PORTS = "PORTS";
    private static final String TABLE_SHIPS = "SHIPS";
    private static final String TABLE_HOURS = "HOURS";
    private static final String TABLE_CONFIG = "CONFIG";
    private static final String TABLE_MANIFEST = "MANIFEST";

    //table manifest
    private static final String MANIFEST_ID = "id";
    private static final String MANIFEST_PEOPLE_ID = "id_people";
    private static final String MANIFEST_ORIGIN = "origin";
    private static final String MANIFEST_DESTINATION = "destination";
    private static final String MANIFEST_ISINSIDE = "is_inside";
    private static final String MANIFEST_PORT = "port";
    private static final String MANIFEST_BOLETUS = "boletus";


    //people
    private static final String PERSON_ID = "id";
    private static final String PERSON_DOCUMENT = "document";
    private static final String PERSON_NAME = "name";
    private static final String PERSON_NATIONALITY = "nationality";
    private static final String PERSON_AGE = "age";
    //routes
    private static final String ROUTE_ID = "id";
    private static final String ROUTE_NAME = "name";
    private static final String ROUTE_SAILING_DATE = "sailing_date";

    //ports
    private static final String PORT_ID = "id";
    private static final String PORT_ID_API = "id_api";
    private static final String PORT_NAME = "name";
    private static final String PORT_IS_IN_MANIFEST = "is_in_manifest";

    //transports
    private static final String SHIP_ID = "id";
    private static final String SHIP_NAME = "name";

    //records
    private static final String RECORD_ID = "id";
    private static final String RECORD_DATETIME = "datetime";
    private static final String RECORD_PERSON_DOC = "person_document";
    private static final String RECORD_PERSON_NAME = "person_name";
    private static final String RECORD_ORIGIN = "origin";
    private static final String RECORD_DESTINATION = "destination";
    private static final String RECORD_PORT_REGISTRY = "port_registry";
    private static final String RECORD_IS_INPUT = "input";
    private static final String RECORD_SYNC = "sync";
    private static final String RECORD_IS_PERMITTED = "permitted";
    private static final String RECORD_TICKET = "ticket";
    private static final String RECORD_REASON = "reason";

    //hours
    private static final String HOUR_ID = "id";
    private static final String HOUR_NAME = "name";

    //Config
    private static final String CONFIG_ROUTE_ID = "route_id";
    private static final String CONFIG_DATE = "date";
    private static final String CONFIG_ROUTE_NAME = "route_name";


    //set table colums
    private static final String[] PEOPLE_COLUMS = {PERSON_ID, PERSON_DOCUMENT, PERSON_NAME, PERSON_NATIONALITY, PERSON_AGE};
    private static final String[] RECORDS_COLUMNS = {RECORD_ID, RECORD_DATETIME, RECORD_PERSON_DOC, RECORD_PERSON_NAME, RECORD_ORIGIN, RECORD_DESTINATION, RECORD_PORT_REGISTRY, RECORD_IS_INPUT, RECORD_SYNC, RECORD_IS_PERMITTED, RECORD_TICKET, RECORD_REASON};
    private static final String[] MANIFEST_COLUMNS = {MANIFEST_ID, MANIFEST_PEOPLE_ID, MANIFEST_ORIGIN, MANIFEST_DESTINATION, MANIFEST_ISINSIDE};
    private static final String[] ROUTES_COLUMNS = {ROUTE_ID, ROUTE_NAME};
    private static final String[] PORTS_COLUMNS = {PORT_ID, PORT_NAME};
    private static final String[] TRANSPORTS_COLUMNS = {SHIP_ID, SHIP_NAME};
    private static final String[] HOURS_COLUMNS = {HOUR_ID, HOUR_NAME};
    private static final String[] CONFIG_COLUMNS = {CONFIG_ROUTE_ID, CONFIG_DATE};

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "NavieraAustral";

    // SQL statement to create the differents tables
    String CREATE_PEOPLE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PEOPLE + " ( " +
            PERSON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            PERSON_DOCUMENT + " TEXT NOT NULL UNIQUE, " +
            PERSON_NAME + " TEXT, " + PERSON_NATIONALITY + " TEXT, " +
            PERSON_AGE + " INTEGER);";

    // "CONSTRAINT "+PERSON_DOCUMENT+" UNIQUE ("+PERSON_DOCUMENT+")); ";

    String CREATE_MANIFEST_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_MANIFEST + " ( " +
            MANIFEST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            MANIFEST_PEOPLE_ID + " TEXT NOT NULL UNIQUE, " +
            MANIFEST_ORIGIN + " TEXT, " +
            MANIFEST_DESTINATION + " TEXT," +
            MANIFEST_ISINSIDE + " INTEGER DEFAULT 0, " +
            MANIFEST_PORT + " INTEGER, " +
            MANIFEST_BOLETUS + " TEXT); ";

    String CREATE_ROUTES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_ROUTES + " ( " +
            ROUTE_ID + " INTEGER PRIMARY KEY, " +
            ROUTE_NAME + " TEXT, " +
            ROUTE_SAILING_DATE + " TEXT);";

    String CREATE_PORTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PORTS + " ( " +
            PORT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            PORT_ID_API + " INTEGER, " +
            PORT_NAME + " TEXT, " +
            PORT_IS_IN_MANIFEST + " TEXT DEFAULT 'FALSE');";

    String CREATE_TRANSPORTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SHIPS + " ( " +
            SHIP_ID + " INTEGER PRIMARY KEY, " +
            SHIP_NAME + " TEXT);";

    String CREATE_RECORDS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_RECORDS + " ( " +
            RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            RECORD_DATETIME + " TEXT, " +
            RECORD_PERSON_DOC + " INTEGER, " +
            RECORD_PERSON_NAME + " TEXT, " +
            RECORD_ORIGIN + " INTEGER, " +
            RECORD_DESTINATION + " INTEGER, " +
            RECORD_PORT_REGISTRY + " TEXT, " +
            RECORD_IS_INPUT + " INTEGER, " +  //input of switch in main 1 embark, 2 landed
            RECORD_SYNC + " INTEGER, " +
            RECORD_IS_PERMITTED + " INTEGER," +
            RECORD_TICKET + " TEXT, " +
            RECORD_REASON + " TEXT); ";

    String CREATE_HOURS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_HOURS + " ( " +
            HOUR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            HOUR_NAME + " TEXT);";

    String CREATE_CONFIG_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CONFIG + " ( " +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            CONFIG_ROUTE_ID + " INTEGER, " +
            CONFIG_ROUTE_NAME + " INTEGER, " +
            CONFIG_DATE + " TEXT);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.enableWriteAheadLogging();
        //first create the tables
        db.execSQL(CREATE_PEOPLE_TABLE);
        db.execSQL(CREATE_ROUTES_TABLE);
        db.execSQL(CREATE_PORTS_TABLE);
        db.execSQL(CREATE_TRANSPORTS_TABLE);
        db.execSQL(CREATE_RECORDS_TABLE);
        db.execSQL(CREATE_HOURS_TABLE);
        db.execSQL(CREATE_CONFIG_TABLE);
        db.execSQL(CREATE_MANIFEST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if it existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PEOPLE);
        //create fresh tables
        this.onCreate(db);
    }

    /**
     * CRUD operations (create "add", read "get", update, delete)
     */

    public void insertJSON(String json, String table) throws JSONException {
        SQLiteDatabase db = getWritableDatabase();
        log_app log = new log_app();
        JSONObject objectJson;
        JSONArray jsonArray;
        switch (table) {
            case "routes":
                if (!json.isEmpty() && json.length() > 3) {
                    objectJson = new JSONObject(json);
                    jsonArray = objectJson.getJSONArray("itinerarios");
                    try {
                        db.beginTransactionNonExclusive();
                        db.delete(TABLE_ROUTES, null, null);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            ContentValues values = new ContentValues();
                            Routes routes = new Routes(jsonArray.getJSONObject(i).getInt("id_itinerario"), jsonArray.getJSONObject(i).getString("nombre_ruta").toUpperCase(),
                                    jsonArray.getJSONObject(i).getString("zarpe"));
                            values.put(ROUTE_ID, routes.getID());
                            values.put(ROUTE_NAME, routes.getName().trim());
                            values.put(ROUTE_SAILING_DATE, routes.getSailing_date());
                            db.insert(TABLE_ROUTES, // table
                                    null, //nullColumnHack
                                    values); // key/value -> keys = column names/ values = column values
                        }
                        db.setTransactionSuccessful();
                    } catch (android.database.SQLException e) {
                        log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
                    } finally {
                        db.endTransaction();
                    }

                } else
                    Log.i("json content", json.toString());
                break;
            case "manifest":
                if (!json.isEmpty()) {
                    objectJson = new JSONObject(json);
                    jsonArray = objectJson.getJSONArray("manifiesto_embarque");
                    try {
                        //db.delete(TABLE_MANIFEST, null, null);
                        //db.execSQL("delete from sqlite_sequence where name='MANIFEST'");
                        db.beginTransactionNonExclusive();
                        for (int i = 0; i < jsonArray.length(); i++) {

                            People people = new People(jsonArray.getJSONObject(i).getString("codigo_pasajero").trim(), jsonArray.getJSONObject(i).getString("nombre_pasajero").toUpperCase(), jsonArray.getJSONObject(i).getString("nacionalidad").toUpperCase(), 0);
                            navieraManifest manifest = new navieraManifest(jsonArray.getJSONObject(i).getString("codigo_pasajero"), jsonArray.getJSONObject(i).getString("origen").toUpperCase(), jsonArray.getJSONObject(i).getString("destino").toUpperCase(), 0);

                            String doc;
                            doc = people.getDocument().toUpperCase();
                            if (people.getDocument().contains("-"))
                                doc = doc.substring(0, doc.length() - 2);
                            String name = removeAccent(people.getName().toUpperCase());
                            db.execSQL("insert or ignore into people(" + PERSON_DOCUMENT + "," + PERSON_NAME + "," + PERSON_NATIONALITY + "," + PERSON_AGE + ") VALUES('" +
                                    doc + "','" + name + "','" + people.getNationality().toUpperCase() + "'," + people.getAge() + ")");
                            db.execSQL("insert or ignore into manifest(" + MANIFEST_PEOPLE_ID + "," + MANIFEST_ORIGIN + "," + MANIFEST_DESTINATION + "," + MANIFEST_ISINSIDE + ") VALUES('" +
                                    doc + "','" + manifest.getOrigin().toUpperCase() + "','" + manifest.getDestination().toUpperCase() + "','" + manifest.getIsInside() + "')");
                        }
                        // finnaly insert fill config table
                        db.setTransactionSuccessful();
                    } catch (JSONException e) {
                        log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
                    } catch (android.database.SQLException e) {
                        log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
                    } finally {
                        db.endTransaction();
                    }
                } else
                    Log.i("error", "Json empty!");
                break;
            case "ports":
                if (!json.isEmpty()) {
                    objectJson = new JSONObject(json);
                    jsonArray = objectJson.getJSONArray("puertos");
                    try {
                        db.beginTransactionNonExclusive();
                        db.execSQL("delete from ports");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            ContentValues values = new ContentValues();
                            Ports port = new Ports(jsonArray.getJSONObject(i).getInt("id_ubicacion"), jsonArray.getJSONObject(i).getString("nombre_ubicacion"));
                            values.put(PORT_ID_API, port.getId());
                            values.put(PORT_NAME, port.getName().trim().toUpperCase());
                            db.insert(TABLE_PORTS, // table
                                    null, //nullColumnHack
                                    values); // key/value -> keys = column names/ values = column values
                        }
                        db.setTransactionSuccessful();
                    } catch (android.database.SQLException e) {
                        log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
                    } finally {
                        db.endTransaction();
                    }
                } else
                    Log.i("error", "Json empty!");
                break;

        }
    }

    public String selectFirst(String Query) {
        String firstElement = "";
        log_app log = new log_app();
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            Cursor cursor = db.rawQuery(Query, null);
            cursor.moveToFirst();
            if (cursor.getCount() == 0)
                return Query = "";
            else
                firstElement = cursor.getString(0);
        } catch (android.database.SQLException e) {
            log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
        }
        return firstElement;
    }

    public String removeAccent(String str) {
        String texto = Normalizer.normalize(str, Normalizer.Form.NFD);
        texto = texto.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        texto = texto.replaceAll("[|?*<\":>+\\[\\]/'`¨´]", "");
        return texto;
    }

    //cris
    public Cursor validatePerson(String rut) {
        //return the person data if this person is in manifest table
        SQLiteDatabase db = this.getWritableDatabase();
        log_app log = new log_app();
        Cursor cursor = null;
        String row = "";
        try {
            cursor = db.rawQuery("select m.id_people,p.name,m.origin,m.destination," +
                    "m.boletus from manifest as m left join people as p on m.id_people=p.document where m.id_people='" + rut + "'", null);
            cursor.moveToFirst();
        } catch (android.database.SQLException e) {
            log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
        }
        return cursor;
    }

    public void add_record(Record record) {
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        log_app log = new log_app();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();

        //values.put(RECORD_ID, record.getId());
        values.put(RECORD_PERSON_DOC, record.getPerson_document());
        values.put(RECORD_PERSON_NAME, record.getPerson_name());
        values.put(RECORD_ORIGIN, record.getOrigin());
        values.put(RECORD_DESTINATION, record.getDestination());
        values.put(RECORD_PORT_REGISTRY, record.getPort_registry());
        values.put(RECORD_IS_INPUT, record.getInput());
        values.put(RECORD_SYNC, record.getSync());
        values.put(RECORD_DATETIME, record.getDatetime());
        values.put(RECORD_IS_PERMITTED, record.getPermitted());
        values.put(RECORD_TICKET, record.getTicket());
        values.put(RECORD_REASON, record.getReason());

        // 3. insert
        try {
            db.beginTransactionNonExclusive();
            db.insert(TABLE_RECORDS, null, values);
            db.setTransactionSuccessful();
        } catch (android.database.SQLException e) {
            log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
        }
        // 4. close
        finally {
            db.endTransaction();
        }
    }

    public List<Record> get_desynchronized_records() {
        log_app log = new log_app();
        Cursor cursor = null;
        List<Record> records = new ArrayList<>();
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getWritableDatabase();
        try {

            // 2. build query
            cursor =
                    db.query(TABLE_RECORDS, // a. table
                            RECORDS_COLUMNS, // b. column names
                            RECORD_SYNC + "=0", // c. selections
                            null, // d. selections args
                            null, // e. group by
                            null, // f. having
                            null, // g. order by
                            null); // h. limit

            // 3. get all
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                Record record = new Record();
                record.setId(cursor.getInt(0));                     //ID
                record.setDatetime(cursor.getString(1));            //DATETIME
                record.setPerson_document(cursor.getString(2));     //PERSON_DOCUMENT
                record.setPerson_name(cursor.getString(3));         //PERSON_NAME
                record.setOrigin(cursor.getString(4));              //ORIGIN
                record.setDestination(cursor.getString(5));         //DESTINATION
                record.setPort_registry(cursor.getString(6));       //PORT_REGISTER
                record.setInput(cursor.getInt(7));                  //INPUT
                record.setSync(cursor.getInt(8));                   //SYNC
                record.setPermitted(cursor.getInt(9));              //PERMITTED
                record.setTicket(cursor.getInt(10));                //MANIFEST TICKET(ONLY IN MANUAL REGISTRATION)
                record.setReason(cursor.getString(11));             //REASON

                records.add(record);
                cursor.moveToNext();
            }
        } catch (android.database.SQLException e) {
            log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
        }
        // 5. return
        return records;
    }

    public void update_record(int id) {
        log_app log = new log_app();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        int i = 0;
        try {
            db.beginTransactionNonExclusive();
            values.put(RECORD_SYNC, 1);
            // 3. updating row
            i = db.update(TABLE_RECORDS, //table
                    values, // column/value
                    RECORD_ID + "=" + id, // where
                    null);
            db.setTransactionSuccessful();
        } catch (android.database.SQLException e) {
            log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
        } finally {
            // 4. close
            db.endTransaction();
        }
        if (i > 0) Log.d("Local Record updated", String.valueOf(id));
        else Log.e("Error updating record", String.valueOf(id));
    }

    public void updatePeopleManifest(String rut, int input) {
        SQLiteDatabase db = this.getWritableDatabase();
        log_app log = new log_app();
        try {
            db.beginTransactionNonExclusive();
            db.execSQL("update manifest set is_inside=" + input + " where id_people='" + rut + "'");
            db.setTransactionSuccessful();
        } catch (android.database.SQLException e) {
            log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public Cursor select(String select) {
        SQLiteDatabase db = this.getWritableDatabase();
        log_app log = new log_app();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(select, null);
            cursor.moveToFirst();
            //cursor.moveToFirst();
        } catch (android.database.SQLException e) {
            log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
        }
        return cursor;
    }

    public void insert(String insert) {
        SQLiteDatabase db = this.getWritableDatabase();
        log_app log = new log_app();
        try {
            db.beginTransactionNonExclusive();
            db.execSQL(insert);
            db.setTransactionSuccessful();
        } catch (android.database.SQLException e) {
            log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
        } finally {
            db.endTransaction();
        }

    }

    public ArrayList<String> selectAsList(String qry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> list = new ArrayList<String>();
        log_app log = new log_app();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(qry, null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                list.add(cursor.getString(0));
            }
        } catch (android.database.SQLException e) {
            log.writeLog(context, "DBHelper", "ERROR", e.getMessage());
        }
        return list;
    }

    public int record_desync_count() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + RECORD_ID + " FROM " + TABLE_RECORDS +
                " WHERE " + RECORD_SYNC + "=0;", null);
        return cursor.getCount();
    }


}