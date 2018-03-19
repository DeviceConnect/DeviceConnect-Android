/*
 RuleEngineDBHelper.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.ruleengine;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.deviceconnect.android.deviceplugin.ruleengine.params.AndRule;
import org.deviceconnect.android.deviceplugin.ruleengine.params.Operation;
import org.deviceconnect.android.deviceplugin.ruleengine.params.Rule;
import org.deviceconnect.android.deviceplugin.ruleengine.params.Trigger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Rule情報を格納するDBヘルパークラス.
 * @author NTT DOCOMO, INC.
 */
class RuleEngineDBHelper {
    /** Define the name of the database. */
    private static final String DB_NAME = "rule_information.db";
    /** Define the version of the database. */
    private static final int DB_VERSION = 1;
    /** Rule情報を格納するテーブル名. */
    private static final String TBL_NAME = "rule_information_tbl";
    /** Rule Service IDを格納するカラム名. */
    private static final String COL_RULE_SERVICE_ID = "rule_service_id";
    /** Rule Service Typeを格納するカラム名. */
    private static final String COL_RULE_SERVICE_TYPE = "rule_service_type";
    /** Rule Enableを格納するカラム名. */
    private static final String COL_RULE_ENABLE = "rule_enable";
    /** Error Statusを格納するカラム名. */
    private static final String COL_ERROR_STATUS = "error_status";
    /** Error Timestampを格納するカラム名. */
    private static final String COL_ERROR_TIMESTAMP = "error_timestamp";
    /** Description を格納するカラム名. */
    private static final String COL_DESCRIPTION = "description";
    /** Trigger構造体を格納するカラム名. */
    private static final String COL_TRIGGERS = "triggers";
    /** Operation構造体を格納するカラム名. */
    private static final String COL_OPERATIONS = "operations";
    /** ANDルール構造体を格納するカラム名. */
    private static final String COL_AND_RULES = "and_rules";

    /** DB管理ヘルパー. */
    private DBHelper mDBHelper;

    /**
     * コンストラクター.
     * @param context Context.
     */
    RuleEngineDBHelper(final Context context) {
        mDBHelper = new DBHelper(context);
    }

    /**
     * ルールデータDB追加.
     * @param rule ルール構造体.
     * @return DB index.
     */
    synchronized long addRuleData(final Rule rule) {
        ContentValues values = new ContentValues();
        values.put(COL_RULE_SERVICE_ID, rule.getRuleServiceId());
        values.put(COL_RULE_SERVICE_TYPE, rule.getRuleServiceType());
        values.put(COL_RULE_ENABLE, rule.isRuleEnable() ? 1 : 0);
        values.put(COL_ERROR_STATUS, rule.getErrorStarus());
        values.put(COL_ERROR_TIMESTAMP, rule.getErrorTimestamp());
        values.put(COL_DESCRIPTION, rule.getRuleDescription());
        Trigger trigger = rule.getTrigger();
        if (trigger != null) {
            try {
                values.put(COL_TRIGGERS, serializeTrigger(trigger));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<Operation> operations = rule.getOperations();
        if (operations != null) {
            try {
                values.put(COL_OPERATIONS, serializeOperations(operations));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        AndRule andRule = rule.getAndRule();
        if (andRule != null) {
            try {
                values.put(COL_AND_RULES, serializeAndRule(andRule));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.insert(TBL_NAME, null, values);
        } finally {
            db.close();
        }
    }

    synchronized long updateRuleData(final Rule rule) {
        ContentValues values = new ContentValues();
        values.put(COL_RULE_SERVICE_ID, rule.getRuleServiceId());
        values.put(COL_RULE_SERVICE_TYPE, rule.getRuleServiceType());
        values.put(COL_RULE_ENABLE, rule.isRuleEnable() ? 1 : 0);
        values.put(COL_ERROR_STATUS, rule.getErrorStarus());
        values.put(COL_ERROR_TIMESTAMP, rule.getErrorTimestamp());
        values.put(COL_DESCRIPTION, rule.getRuleDescription());
        Trigger trigger = rule.getTrigger();
        if (trigger != null) {
            try {
                values.put(COL_TRIGGERS, serializeTrigger(trigger));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<Operation> operations = rule.getOperations();
        if (operations != null) {
            try {
                values.put(COL_OPERATIONS, serializeOperations(operations));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        AndRule andRule = rule.getAndRule();
        if (andRule != null) {
            try {
                values.put(COL_AND_RULES, serializeAndRule(andRule));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String whereClause = COL_RULE_SERVICE_ID + "=?";
        String[] whereArgs = {
                rule.getRuleServiceId()
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.update(TBL_NAME, values, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    /**
     * 指定ルールサービスIDのルール構造体をDBから取得.
     * @param ruleServiceId ルールサービスID.
     * @return ルール構造体.
     */
    public synchronized Rule findRuleDataByRuleServiceId(final String ruleServiceId) {
        Rule rule;
        String SQL_SELECT = "SELECT * FROM " + TBL_NAME + " WHERE "
                + COL_RULE_SERVICE_ID + "=? " + ";";
        String[] whereArgs = {
                ruleServiceId
        };

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_SELECT, whereArgs);
        if (cursor.moveToFirst()) {
            rule = new Rule(cursor.getString(cursor.getColumnIndex(COL_RULE_SERVICE_TYPE)));
            rule.setRuleServiceId(cursor.getString(cursor.getColumnIndex(COL_RULE_SERVICE_ID)));
            rule.setRuleEnable(cursor.getInt(cursor.getColumnIndex(COL_RULE_ENABLE)) == 1);
            String errorStatus = cursor.getString(cursor.getColumnIndex(COL_ERROR_STATUS));
            if (errorStatus != null) {
                rule.setErrorStarus(errorStatus);
            }
            String errorTimestamp = cursor.getString(cursor.getColumnIndex(COL_ERROR_TIMESTAMP));
            if (errorTimestamp != null) {
                rule.setErrorTimestamp(errorTimestamp);
            }
            String description = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION));
            if (description != null) {
                rule.setRuleDescription(description);
            }
            byte[] stream = cursor.getBlob(cursor.getColumnIndex(COL_TRIGGERS));
            if (stream != null) {
                try {
                    rule.setTrigger(deSerializeTrigger(stream));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            stream = cursor.getBlob(cursor.getColumnIndex(COL_OPERATIONS));
            if (stream != null) {
                try {
                    rule.setOperations(deSerializeOperations(stream));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            stream = cursor.getBlob(cursor.getColumnIndex(COL_AND_RULES));
            if (stream != null) {
                try {
                    rule.setAndRule(deSerializeAndRule(stream));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
            return rule;
        } else {
            cursor.close();
            db.close();
            return null;
        }
    }

    /**
     * 該当ルール構造体をDBから削除.
     * @param rule ルール構造体.
     * @return 削除されたインデックス、または0(エラー).
     */
    public synchronized long removeRuleData(final Rule rule) {
        return removeRuleDataByRuleServiceId(rule.getRuleServiceId());
    }

    /**
     * 該当ルール構造体をDBから削除.
     * @param ruleServiceId ルールサービスID.
     * @return 削除されたインデックス、または0(エラー).
     */
    synchronized long removeRuleDataByRuleServiceId(final String ruleServiceId) {
        String whereClause = COL_RULE_SERVICE_ID + "=?";
        String[] whereArgs = {
                ruleServiceId
        };

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            return db.delete(TBL_NAME, whereClause, whereArgs);
        } finally {
            db.close();
        }
    }

    /**
     * DBに登録されている全てのルール構造体を取得.
     * @return ルール構造体リスト.
     */
    synchronized List<Rule> getRules() {
        String sql = "SELECT * FROM " + TBL_NAME;
        String[] selectionArgs = {};

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, selectionArgs);

        List<Rule> rules = new ArrayList<>();
        boolean next = cursor.moveToFirst();
        while (next) {
            Rule rule = new Rule(cursor.getString(cursor.getColumnIndex(COL_RULE_SERVICE_TYPE)));
            rule.setRuleServiceId(cursor.getString(cursor.getColumnIndex(COL_RULE_SERVICE_ID)));
            rule.setRuleEnable(cursor.getInt(cursor.getColumnIndex(COL_RULE_ENABLE)) == 1);
            String errorStatus = cursor.getString(cursor.getColumnIndex(COL_ERROR_STATUS));
            if (errorStatus != null) {
                rule.setErrorStarus(errorStatus);
            }
            String errorTimestamp = cursor.getString(cursor.getColumnIndex(COL_ERROR_TIMESTAMP));
            if (errorTimestamp != null) {
                rule.setErrorTimestamp(errorTimestamp);
            }
            String description = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION));
            if (description != null) {
                rule.setRuleDescription(description);
            }
            byte[] stream = cursor.getBlob(cursor.getColumnIndex(COL_TRIGGERS));
            if (stream != null) {
                try {
                    rule.setTrigger(deSerializeTrigger(stream));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            stream = cursor.getBlob(cursor.getColumnIndex(COL_OPERATIONS));
            if (stream != null) {
                try {
                    rule.setOperations(deSerializeOperations(stream));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            stream = cursor.getBlob(cursor.getColumnIndex(COL_AND_RULES));
            if (stream != null) {
                try {
                    rule.setAndRule(deSerializeAndRule(stream));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            rules.add(rule);
            next = cursor.moveToNext();
        }
        cursor.close();
        return rules;
    }

    /**
     * DBHelper.
     */
    private static class DBHelper extends SQLiteOpenHelper {
        DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            createDB(db);
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DB_NAME);
            createDB(db);
        }

        private void createDB(final SQLiteDatabase db) {
            String sql = "CREATE TABLE " + TBL_NAME + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY, "
                    + COL_RULE_SERVICE_ID + " TEXT NOT NULL,"
                    + COL_RULE_SERVICE_TYPE + " TEXT NOT NULL,"
                    + COL_RULE_ENABLE + " INTEGER,"
                    + COL_ERROR_STATUS + " TEXT,"
                    + COL_ERROR_TIMESTAMP + " TEXT,"
                    + COL_DESCRIPTION + " TEXT,"
                    + COL_TRIGGERS + " BLOB,"
                    + COL_OPERATIONS + " BLOB,"
                    + COL_AND_RULES + " BLOB"
                    + ");";
            db.execSQL(sql);
        }
    }

    /**
     * トリガー構造体用シリアライズ.
     * @param trigger トリガー構造体.
     * @return Byte配列.
     * @throws IOException IO例外.
     */
    private byte[] serializeTrigger(Trigger trigger) throws IOException {
        // Byte配列への出力ストリーム.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // オブジェクトをバイト配列に変換するためのクラス.
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        // シリアライズ.
        oos.writeObject(trigger);
        // Byte配列返却.
        return baos.toByteArray();
    }

    /**
     * オペレーション構造体リスト用シリアライズ.
     * @param operations オペレーション構造体リスト.
     * @return Byte配列.
     * @throws IOException IO例外.
     */
    private byte[] serializeOperations(List<Operation> operations) throws IOException {
        // Byte配列への出力ストリーム.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // オブジェクトをバイト配列に変換するためのクラス.
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        // シリアライズ.
        oos.writeObject(operations);
        // Byte配列返却.
        return baos.toByteArray();
    }

    /**
     * ANDルール構造体用シリアライズ.
     * @param andRule ANDルール構造体.
     * @return Byte配列.
     * @throws IOException IO例外.
     */
    private byte[] serializeAndRule(AndRule andRule) throws IOException {
        // Byte配列への出力ストリーム.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // オブジェクトをバイト配列に変換するためのクラス.
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        // シリアライズ.
        oos.writeObject(andRule);
        // Byte配列返却.
        return baos.toByteArray();
    }

    /**
     * トリガー構造体用デシリアライズ.
     * @param stream DBからのストリーム
     * @return トリガー構造体.
     * @throws IOException IO例外.
     * @throws ClassNotFoundException クラス不存在例外.
     */
    private Trigger deSerializeTrigger(byte[] stream) throws IOException, ClassNotFoundException {
        // DBからのストリームを入力.
        ByteArrayInputStream bais = new ByteArrayInputStream(stream);
        // デシリアライズクラスとストリーム入力を連結.
        ObjectInputStream ois = new ObjectInputStream(bais);
        // デシリアライズ.
        return (Trigger) ois.readObject();
    }

    /**
     * オペレーション構造体リスト用デシリアライズ.
     * @param stream DBからのストリーム
     * @return オペレーション構造体リスト.
     * @throws IOException IO例外.
     * @throws ClassNotFoundException クラス不存在例外.
     */
    private List<Operation> deSerializeOperations(byte[] stream) throws IOException, ClassNotFoundException {
        // DBからのストリームを入力.
        ByteArrayInputStream bais = new ByteArrayInputStream(stream);
        // デシリアライズクラスとストリーム入力を連結.
        ObjectInputStream ois = new ObjectInputStream(bais);
        // デシリアライズ.
        return (List<Operation>) ois.readObject();
    }

    /**
     * ANDルール構造体用デシリアライズ.
     * @param stream DBからのストリーム
     * @return ANDルール構造体.
     * @throws IOException IO例外.
     * @throws ClassNotFoundException クラス不存在例外.
     */
    private AndRule deSerializeAndRule(byte[] stream) throws IOException, ClassNotFoundException {
        // DBからのストリームを入力.
        ByteArrayInputStream bais = new ByteArrayInputStream(stream);
        // デシリアライズクラスとストリーム入力を連結.
        ObjectInputStream ois = new ObjectInputStream(bais);
        // デシリアライズ.
        return (AndRule) ois.readObject();
    }
}
