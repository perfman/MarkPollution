package com.project.markpollution.CustomAdapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.project.markpollution.Interfaces.OnItemClickListener;
import com.project.markpollution.Objects.PollutionPoint;
import com.project.markpollution.R;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Hung on 27-Oct-16.
 */

public class ReportMgrRecyclerViewAdapter extends RecyclerView.Adapter<ReportMgrRecyclerViewAdapter.ReportRecyclerViewHolder> {
    private Context context;
    private List<PollutionPoint> listPo = new ArrayList<>();
    private String url_CountCommentByPo = "http://indi.com.vn/dev/markpollution/CountCommentByPo.php?id_po=";
    private String url_SumRateByPo = "http://indi.com.vn/dev/markpollution/SumRateByPo.php?id_po=";
    private OnItemClickListener onItemClickListener;    // interface to store PollutionPoint object

    public ReportMgrRecyclerViewAdapter(Context context, List<PollutionPoint> listPo) {
        this.context = context;
        this.listPo = listPo;
    }

    // Reference to layout then pass view's layout into ReportRecyclerViewHolder object
    @Override
    public ReportRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_recyclerview_reportmgr, parent, false);
        return new ReportRecyclerViewHolder(view);  // from ViewHolder object it'll reference its child view
    }

    // Bind data into ViewHolder by position
    @Override
    public void onBindViewHolder(ReportRecyclerViewHolder holder, int position) {
        final PollutionPoint curPo = listPo.get(position);  // current pollutionPoint by position
        // pass value into holders
        holder.tvTitle.setText(curPo.getTitle());
        holder.tvDesc.setText(curPo.getDesc());
        holder.tvTime.setText(formatDateTime(curPo.getTime()));
        // set Picture into holder
        Picasso.with(context).load(Uri.parse(curPo.getImage())).placeholder(R.drawable.placeholder).into(holder.ivPicture);
        // set countComment into holder
        setCountCommentByPo(curPo.getId(), holder.tvComment);
        // set sumRate into holder
        setSumRateByPo(curPo.getId(), holder.tvRate);

        // Initialize OnClickListener
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(curPo); // pass pollutionPoint object into interface's method
            }
        };
        holder.container.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return listPo.size();
    }

    class ReportRecyclerViewHolder extends RecyclerView.ViewHolder{
        ImageView ivPicture;
        TextView tvTitle, tvDesc, tvTime, tvRate, tvComment;
        RelativeLayout container;

        public ReportRecyclerViewHolder(View itemView) {
            super(itemView);
            ivPicture = (ImageView) itemView.findViewById(R.id.imageViewPicReportMgr);
            tvTitle = (TextView) itemView.findViewById(R.id.textViewTitleReportMgr);
            tvDesc = (TextView) itemView.findViewById(R.id.textViewDescReportMgr);
            tvTime = (TextView) itemView.findViewById(R.id.textViewTimeReportMgr);
            tvComment = (TextView) itemView.findViewById(R.id.textViewCommentReportMgr);
            tvRate = (TextView) itemView.findViewById(R.id.textViewRateReportMgr);
            container = (RelativeLayout) itemView.findViewById(R.id.container_reportMgr);
        }
    }

    private String formatDateTime(String time){
        SimpleDateFormat originFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat resultFormat = new SimpleDateFormat("hh:mm:ss dd/MM/yyyy");

        Date datetime = null;
        try {
            datetime = originFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return resultFormat.format(datetime);
    }

    private void setCountCommentByPo(String id_po, final TextView ivHolder){
        StringRequest strReq = new StringRequest(Request.Method.GET, url_CountCommentByPo +
                id_po, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.equals("Count comment failure")){
                    ivHolder.setText(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Volley", error.getMessage());
            }
        });

        Volley.newRequestQueue(context).add(strReq);
    }

    private void setSumRateByPo(String id_po, final TextView tvHolder){
        StringRequest strReq = new StringRequest(Request.Method.GET, url_SumRateByPo + id_po , new
                Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                tvHolder.setText(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Volley", error.getMessage());
            }
        });

        Volley.newRequestQueue(context).add(strReq);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
}
