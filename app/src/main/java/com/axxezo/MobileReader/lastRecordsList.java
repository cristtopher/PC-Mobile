package com.axxezo.MobileReader;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class lastRecordsList extends ListActivity implements AdapterView.OnItemSelectedListener {
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ArrayList<Cards> users;
    private customViewPeople adapter;
    private int manifestCount;
    private int PendingCount;
    private int EmbarkedCount;
    private int LandedCount;
    private int ManualSellCount;
    private Spinner combo_origin;
    private Spinner combo_destination;
    private cardsSpinnerAdapter spinner_adapter_origin;
    private cardsSpinnerAdapter spinner_adapter_destination;
    private String spinner_destination_selected;//default first item in combobox
    private String spinner_origin_selected;//default first item in combobox
    private TextView txt_view_origin;
    private TextView txt_view_destination;
    private Vibrator mVibrator;
    private EditText find;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_records_list);
        recyclerView = (RecyclerView) findViewById(R.id.peopleList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        users = new ArrayList<>();
        adapter = new customViewPeople(users);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        //adapter.getFilter().filter();
        combo_origin = (Spinner) findViewById(R.id.spinner_origin);
        combo_destination = (Spinner) findViewById(R.id.spinner_destination);
        txt_view_origin = (TextView) findViewById(R.id.txt_view_origin);
        txt_view_destination = (TextView) findViewById(R.id.txt_view_destination);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        find = (EditText) findViewById(R.id.editttext_search);
        DatabaseHelper db = DatabaseHelper.getInstance(this);

        //fill origin combobox
        ArrayList<String> listOriginAndDestination = db.selectAsList("select name from ports order by name desc", 0);
        if (listOriginAndDestination != null)
            if (listOriginAndDestination.isEmpty())
                listOriginAndDestination.add("");
            else
                listOriginAndDestination.add(0, "< TODOS >");
        spinner_adapter_origin = new cardsSpinnerAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOriginAndDestination);
        spinner_adapter_origin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        combo_origin.setAdapter(spinner_adapter_origin);
        spinner_origin_selected = combo_origin.getItemAtPosition(0).toString();
        combo_origin.setOnItemSelectedListener(this);

        //fill destination with same combinations of ports
        spinner_adapter_destination = new cardsSpinnerAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOriginAndDestination);
        spinner_adapter_destination.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        combo_destination.setAdapter(spinner_adapter_destination);
        spinner_destination_selected = combo_destination.getItemAtPosition(0).toString();
        combo_destination.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
                spinner_destination_selected = parent.getItemAtPosition(position).toString();

                String spinner_origin_name_selection = spinner_origin_selected.equals("< TODOS >") ? spinner_origin_selected : db.selectFirst("select id_mongo from ports where name='" + spinner_origin_selected + "'");
                String spinner_destination_name_selection = spinner_destination_selected.equals("< TODOS >") ? spinner_destination_selected : db.selectFirst("select id_mongo from ports where name='" + spinner_destination_selected + "'");
                //Here we use the Filtering Feature which we implemented in our Adapter class.
                adapter.getFilter().filter((CharSequence) spinner_origin_name_selection + "," + spinner_destination_name_selection, new Filter.FilterListener() {
                    @Override
                    public void onFilterComplete(int count) {
                        adapter.notifyDataSetChanged();
                        recyclerView.setAdapter(adapter);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        addPersonCards();
        getStatusFromManifest(1, spinner_origin_selected, spinner_destination_selected);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_lastRecords);
        //FloatingActionButton fab_search = (FloatingActionButton) findViewById(R.id.fab_search);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = "Origen: " + spinner_origin_selected + " Destino: " + spinner_destination_selected +
                        "\nTotal Tramo: " + getStatusFromManifest(1, spinner_origin_selected, spinner_destination_selected) +
                        "\nEmbarcados: " + getStatusFromManifest(3, spinner_origin_selected, spinner_destination_selected) +
                        "\nDesembarcados: " + getStatusFromManifest(4, spinner_origin_selected, spinner_destination_selected) +
                        "\nPendientes : " + getStatusFromManifest(2, spinner_origin_selected, spinner_destination_selected) +
                        "\nCompras a bordo : " + getStatusFromManifest(5, spinner_origin_selected, spinner_destination_selected);
                Snackbar snack = Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                        .setAction("Action", null);
                View view1 = snack.getView();
                TextView information = (TextView) view1.findViewById(android.support.design.R.id.snackbar_text);
                information.setTextColor(Color.WHITE);
                information.setMaxLines(6);
                snack.show();
            }
        });
        /*fab_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVibrator.vibrate(100);
                find.setVisibility(View.GONE);
                find.setVisibility(View.VISIBLE);
                RelativeLayout coor = (RelativeLayout) findViewById(R.id.relative_layout);
                ViewGroup.LayoutParams params = coor.getLayoutParams();
// Changes the height and width to the specified *pixels*
                params.width=500;
                params.height =100;
                coor.setLayoutParams(params);
               /* CoordinatorLayout coor = (CoordinatorLayout) findViewById(R.id.content_last_records_list);
                find.setLayoutParams(new LinearLayout.LayoutParams(
                        CoordinatorLayout.LayoutParams.MATCH_PARENT,
                        CoordinatorLayout.LayoutParams.WRAP_CONTENT));
                CoordinatorLayout.LayoutParams params1= new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT);

                find.setText("asdasdasdasdasd");
                if (coor.getChildCount() == 8) {
                  //  params1.rule
                    coor.addView(find);
                }
                Log.e("lenght"," "+coor.getChildCount());*/


       //     }
        //});

    }

    private void addPersonCards() {
        try {
            DatabaseHelper db = DatabaseHelper.getInstance(this);
            Cursor c = db.select("select m.id_people,p.name,p.nationality,m.origin,m.destination,m.is_inside from manifest as m left join people as p on m.id_people=p.document");
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        String DNI = c.getString(c.getColumnIndex("id_people"));
                        String Name = c.getString(c.getColumnIndex("name"));
                        // String nationality = c.getString(c.getColumnIndex("nationality"));
                        int isInput = c.getInt(c.getColumnIndex("is_inside"));
                        String origin = c.getString(c.getColumnIndex("origin"));
                        String destination = c.getString(c.getColumnIndex("destination"));
                        users.add(new Cards(DNI, Name, isInput, origin, destination));
                    } while (c.moveToNext());
                }
            }
            if (c != null)
                c.close();
        } catch (SQLiteException se) {
            Log.e(getClass().getSimpleName(), "Could not create or Open the database");
        }
    }

    public int getStatusFromManifest(int position, String origin, String destination) {
        Cursor select_counts = null;
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        if (origin.equals("< TODOS >") && destination.equals("< TODOS >")) {
            select_counts = db.select("select (select count(*) from manifest)," +
                    "(select count(*) from manifest where is_inside=0)," +
                    "(select count(*) from manifest where is_inside=1)," +
                    "(select count(*) from manifest where is_inside=2)," +
                    "(select count(*) from manifest where is_inside=1 and boletus is not null)");
        } else if (origin.equals("< TODOS >") && !destination.equals("< TODOS >")) {
            select_counts = db.select("select (select count(*) from manifest where destination=(select id_mongo from ports where name='" + destination + "'))," +
                    "(select count(*) from manifest where is_inside=0 and destination=(select id_mongo from ports where name='" + destination + "'))," +
                    "(select count(*) from manifest where is_inside=1 and destination=(select id_mongo from ports where name='" + destination + "'))," +
                    "(select count(*) from manifest where is_inside=2 and destination=(select id_mongo from ports where name='" + destination + "'))," +
                    "(select count(*) from manifest where is_inside=1 and boletus is not null and destination=(select id_mongo from ports where name='\" + destination + \"'))");
        } else if (!origin.equals("< TODOS >") && destination.equals("< TODOS >")) {
            select_counts = db.select("select (select count(*) from manifest where origin=(select id_mongo from ports where name='" + origin + "'))," +
                    "(select count(*) from manifest where is_inside=0 and origin=(select id_mongo from ports where name='" + origin + "'))," +
                    "(select count(*) from manifest where is_inside=1 and origin=(select id_mongo from ports where name='" + origin + "'))," +
                    "(select count(*) from manifest where is_inside=2 and origin=(select id_mongo from ports where name='" + origin + "'))," +
                    "(select count(*) from manifest where is_inside=1 and boletus is not null and origin=(select id_mongo from ports where name='\" + origin + \"'))");
        } else { //last case when
            select_counts = db.select("select (select count(*) from manifest where origin=(select id_mongo from ports where name='" + origin + "') and destination=(select id_mongo from ports where name='" + destination + "'))," +
                    "(select count(*) from manifest where is_inside=0 and origin=(select id_mongo from ports where name='" + origin + "') and destination=(select id_mongo from ports where name='" + destination + "'))," +
                    "(select count(*) from manifest where is_inside=1 and origin=(select id_mongo from ports where name='" + origin + "') and destination=(select id_mongo from ports where name='" + destination + "'))," +
                    "(select count(*) from manifest where is_inside=2 and origin=(select id_mongo from ports where name='" + origin + "') and destination=(select id_mongo from ports where name='" + destination + "'))," +
                    "(select count(*) from manifest where is_inside=1 and boletus is not null and origin=(select id_mongo from ports where name='\" + origin + \"') and destination=(select id_mongo from ports where name='\" + destination + \"'))");
        }

        int count = 0;
        if (select_counts != null && select_counts.getCount() > 0) {
            manifestCount = select_counts.getInt(0);
            PendingCount = select_counts.getInt(1);
            EmbarkedCount = select_counts.getInt(2);
            LandedCount = select_counts.getInt(3);
            ManualSellCount = select_counts.getInt(4);
        }
        switch (position) {
            case 1:
                count = manifestCount;
                break;
            case 2:
                count = PendingCount;
                break;
            case 3:
                count = EmbarkedCount;
                break;
            case 4:
                count = LandedCount;
                break;
            case 5:
                count = ManualSellCount;
                break;
        }
        if (select_counts != null)
            select_counts.close();

        return count;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Cards cards = new Cards();
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        spinner_origin_selected = parent.getItemAtPosition(position).toString();
        String spinner_origin_name_selection = spinner_origin_selected.equals("< TODOS >") ? spinner_origin_selected : db.selectFirst("select id_mongo from ports where name='" + spinner_origin_selected + "'");
        String spinner_destination_name_selection = spinner_destination_selected.equals("< TODOS >") ? spinner_destination_selected : db.selectFirst("select id_mongo from ports where name='" + spinner_destination_selected + "'");
        Log.e("mongo_id origin", spinner_origin_name_selection);
        Log.e("mongo_id destination", spinner_destination_name_selection);
        //Here we use the Filtering Feature which we implemented in our Adapter class.
        if (find.getVisibility() == View.GONE || find.getVisibility() == View.INVISIBLE)
            adapter.getFilter().filter((CharSequence) spinner_origin_name_selection + "," + spinner_destination_name_selection, new Filter.FilterListener() {
                @Override
                public void onFilterComplete(int count) {
                    adapter.notifyDataSetChanged();
                    recyclerView.setAdapter(adapter);
                }
            });
        if (find.getVisibility() == View.VISIBLE)
            adapter.getFilter().filter((CharSequence) find.getText() + ",", new Filter.FilterListener() {
                @Override
                public void onFilterComplete(int count) {
                    adapter.notifyDataSetChanged();
                    recyclerView.setAdapter(adapter);
                }
            });

    }
    
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
