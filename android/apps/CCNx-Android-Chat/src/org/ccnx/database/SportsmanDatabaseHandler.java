package org.ccnx.database;

import java.util.ArrayList;
import java.util.List;

import org.ccnx.android.apps.ui.interfaces.transferObjects.SportsmanData;
import org.ccnx.android.apps.ui.interfaces.transferObjects.TrainingPlan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SportsmanDatabaseHandler extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "sportsmanDB.db";
	private static final String PROFILE_TABLE = "profiles";
	private static final String TRAINING_TABLE = "trainings";
	private static final String COLUMN_ID = "_id";
	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_SURNAME = "surname";
	private static final String COLUMN_HEIGHT = "height";
	private static final String COLUMN_WEIGHT = "weight";
	private static final String COLUMN_TRAINING_NAME = "name";
	private static final String COLUMN_TRAINING_DESCRITION = "description";
	private static final String COLUMN_TRAINING_SELF_RANK = "selfRank";
	private static final String COLUMN_TRAINING_RANK = "rank";
	private static final String COLUMN_TRAINING_COMMENT = "comment";

	public SportsmanDatabaseHandler(Context context, String name,
			SQLiteDatabase.CursorFactory factory, int version) {
		super(context, DATABASE_NAME, factory, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	      db.execSQL(generateProfileTableSql());
	      db.execSQL(generateTrainingPlansTableSql());
	}
	
	private String generateProfileTableSql() {
		return "CREATE TABLE " + PROFILE_TABLE + "("
				+ COLUMN_ID + " INTEGER PRIMARY KEY,"
				+ COLUMN_NAME + " VARCHAR,"
				+ COLUMN_SURNAME + " VARCHAR,"
				+ COLUMN_HEIGHT + " VARCHAR,"
				+ COLUMN_WEIGHT + " VARCHAR)";
	}	
	
	private String generateTrainingPlansTableSql() {
		return "CREATE TABLE " + TRAINING_TABLE + "("
				+ COLUMN_ID + " INTEGER PRIMARY KEY,"
				+ COLUMN_TRAINING_NAME + " VARCHAR,"
				+ COLUMN_TRAINING_DESCRITION + " VARCHAR,"
				+ COLUMN_TRAINING_SELF_RANK + " VARCHAR,"
				+ COLUMN_TRAINING_RANK + " VARCHAR,"
				+ COLUMN_TRAINING_COMMENT + " VARCHAR)";
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		dropTable(db, PROFILE_TABLE);
		dropTable(db, TRAINING_TABLE);
		onCreate(db);
	}
	
	private void dropTable(SQLiteDatabase db, String tableName) {
		db.execSQL("DROP TABLE IF EXISTS " + tableName);
	}
	
	public void addProfile(SportsmanData profile) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, profile.getName());
        values.put(COLUMN_SURNAME, profile.getSurname());
        values.put(COLUMN_HEIGHT, profile.getHeight());
        values.put(COLUMN_WEIGHT, profile.getWeight());
 
        insertIntoTable(PROFILE_TABLE, values);
	}
	
	public void addTrainings(List<TrainingPlan> plans) {
		// so so so long way to perfection in this method :(
		deleteAllFromTable(TRAINING_TABLE);
		for(TrainingPlan plan : plans) {
			ContentValues values = new ContentValues();
			values.put(COLUMN_TRAINING_NAME, plan.getName());
			values.put(COLUMN_TRAINING_DESCRITION, plan.getDescription());
			values.put(COLUMN_TRAINING_SELF_RANK, plan.getSelfRank());
			insertIntoTable(TRAINING_TABLE, values);
		}
	}
	
	public void addTraining(TrainingPlan plan) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_TRAINING_NAME, plan.getName());
		values.put(COLUMN_TRAINING_DESCRITION, plan.getDescription());
		values.put(COLUMN_TRAINING_SELF_RANK, plan.getSelfRank());
		insertIntoTable(TRAINING_TABLE, values);
	}
	
	private void deleteAllFromTable(String tableName) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(tableName, null, null);
	}
	
	private void insertIntoTable(String tableName, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(PROFILE_TABLE, null, values);
        db.close();	
	}
	
	public SportsmanData getLatestProfile() {
		String query = "Select * FROM " + PROFILE_TABLE;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);
		
		if(cursor.moveToFirst()) {
			SportsmanData profile = new SportsmanData(
					cursor.getString(1),
					cursor.getString(2),
					cursor.getString(3),
					cursor.getString(4));
			cursor.close();
			db.close();
			return profile;
		}
		return new SportsmanData("", "", "", "");
	}
	
	public TrainingPlan getLastTraining() {
		List<TrainingPlan> plans = getTrainings();
		if(plans != null && !plans.isEmpty()) {
			return plans.get(0);			
		}
		return new TrainingPlan("", "", "", "", "");
	}
	
	public List<TrainingPlan> getTrainings() {
		String query = "Select * FROM " + TRAINING_TABLE;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(query, null);
		List<TrainingPlan> trainings = new ArrayList<TrainingPlan>();
		
		cursor.moveToFirst();
		while(cursor.moveToNext()) {
			TrainingPlan trainingPlan = new TrainingPlan(
					cursor.getString(1),
					cursor.getString(2),
					cursor.getString(3),
					cursor.getString(4),
					cursor.getString(5));
			trainings.add(trainingPlan);
		}
		cursor.close();
		db.close();
		return trainings;
	}

}
