package com.project.markpollution.CustomAdapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.project.markpollution.Objects.Comment;
import com.project.markpollution.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Hung on 22-Oct-16.
 */

public class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewAdapter.CommentRecyclerViewHolder> {
    private List<Comment> listComment;
//    private List<User> listUser = new ArrayList<>();
    private Context mContext;
    private String id_po;
//    private String url_RetrieveUserByComment = "http://2dev4u.com/dev/markpollution/RetrieveUserByComment.php?id_po=";
    private String url_RetrieveUserById = "http://indi.com.vn/dev/markpollution/RetrieveUserById.php?id_user=";

    public CommentRecyclerViewAdapter(Context mContext, List<Comment> listComment/*, String id_po*/) {
        this.mContext = mContext;
        this.listComment = listComment;
//        this.id_po = id_po;
    }

    @Override
    public CommentRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_recyclerview_comment, parent, false);
        return new CommentRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CommentRecyclerViewHolder holder, int position) {
        final Comment currComment = listComment.get(position);
        holder.tvComment.setText(currComment.getComment());
        holder.tvTime.setText(formatDateTime(currComment.getTime()));

        // set avatar & name into Holders
        setAvatarAndName(currComment.getId_user(), holder.ivAvatar, holder.tvName);
    }

    @Override
    public int getItemCount() {
        return listComment.size();
    }

    class CommentRecyclerViewHolder extends RecyclerView.ViewHolder{
        ImageView ivAvatar;
        TextView tvName, tvTime, tvComment;
        public CommentRecyclerViewHolder(View itemView) {
            super(itemView);
            ivAvatar = (ImageView) itemView.findViewById(R.id.imageViewAvatarComment);
            tvName = (TextView) itemView.findViewById(R.id.textViewNameComment);
            tvTime = (TextView) itemView.findViewById(R.id.textViewTimeComment);
            tvComment = (TextView) itemView.findViewById(R.id.textViewComment);
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

    private void setAvatarAndName(String UserID, final ImageView avatar, final TextView name){
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url_RetrieveUserById + UserID, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getString("status").equals("success")){
                        JSONArray arr = response.getJSONArray("response");
                        JSONObject user = arr.getJSONObject(0);
                        String _avatar = user.getString("avatar");
                        String _name = user.getString("name_user");

                        // set avatar into holder
                        Picasso.with(mContext).load(Uri.parse(_avatar)).resize(70,70).transform(new CircleTransform()).into(avatar);
                        // set name into holder
                        name.setText(_name);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.getMessage());
            }
        });

        Volley.newRequestQueue(mContext).add(objectRequest);
    }

//    private void retrieveUsers(){
//        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url_RetrieveUsers, null, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                List<User> listUser = new ArrayList<>();
//                try {
//                    if(response.getString("status").equals("success")){
//                        JSONArray arr = response.getJSONArray("response");
//                        for(int i=0; i<arr.length(); i++){
//                            JSONObject user = arr.getJSONObject(i);
//                            listUser.add(new User(user.getString("id_user"), user.getString("name_user"), user.getString("email"), user.getString("avatar"), user.getBoolean("is_admin")));
//                        }
//
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e("Volley", error.getMessage());
//            }
//        });
//
//        Volley.newRequestQueue(mContext).add(objectRequest);
//    }

}
