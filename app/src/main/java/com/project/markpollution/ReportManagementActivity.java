package com.project.markpollution;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.project.markpollution.CustomAdapter.ReportMgrRecyclerViewAdapter;
import com.project.markpollution.Interfaces.OnItemClickListener;
import com.project.markpollution.Objects.PollutionPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ReportManagementActivity extends AppCompatActivity {
    private RecyclerView recyclerViewReportMgr;
    private String url_RetrievePollutionByUserID = "http://indi.com.vn/dev/markpollution/RetrievePollutionsByUserID.php?id_user=";
    private List<PollutionPoint> listPoByUserID;
    private ReportMgrRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_management);

        initView();
        fetchPoByUser();
    }

    private void initView() {
        // reference to recyclerView
        recyclerViewReportMgr = (RecyclerView) findViewById(R.id.recyclerViewReportMgr);
        // initialize layout for recyclerView
        LinearLayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        // set layout for recyclerView
        recyclerViewReportMgr.setLayoutManager(layout);
    }

    private void fetchPoByUser() {
        // request PollutionPointByUserID from server then pass into List<PollutionPoint>
        JsonObjectRequest ObjReq = new JsonObjectRequest(Request.Method.GET,
                url_RetrievePollutionByUserID + getUserID(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        listPoByUserID = new ArrayList<>();
                        try {
                            if (response.getString("status").equals("success")) {
                                JSONArray arr = response.getJSONArray("response");
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject po = arr.getJSONObject(i);
                                    listPoByUserID.add(new PollutionPoint(po.getString("id_po"),
                                            po.getString("id_cate"), po.getString("id_user"), po
                                            .getDouble("lat"), po.getDouble("lng"), po.getString
                                            ("title"), po.getString("description"), po.getString
                                            ("image"), po.getString("time")));
                                }
                                // show data on RecyclerView
                                loadListReport(listPoByUserID);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.getMessage());
                Toast.makeText(ReportManagementActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Volley.newRequestQueue(this).add(ObjReq);
    }

    // Get UserID which is stored in ShareReferences
    private String getUserID() {
        SharedPreferences sharedPreferences = getSharedPreferences("sharedpref_id_user", MODE_PRIVATE);
        return sharedPreferences.getString("sharedpref_id_user", "");
    }

    private void loadListReport(List<PollutionPoint> list) {
        // initialize adapter then pass list<Po> into it
        adapter = new ReportMgrRecyclerViewAdapter(this, list);
        // set adapter for recyclerView
        recyclerViewReportMgr.setAdapter(adapter);
        // Call method setOnItemClickListener of adapter to override onItemClick method
        // Method's parameter is PollutionPoint object when user click on specific recyclerView
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(PollutionPoint po) {
                // PollutionPoint's ID then send into DetailActivity
                Intent intent = new Intent(ReportManagementActivity.this, DetailReportActivity.class);
                intent.putExtra("id_po", po.getId());
                startActivity(intent);
            }
        });
    }
}
