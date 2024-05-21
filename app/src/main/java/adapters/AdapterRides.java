package adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.ParseException;
import android.net.Uri;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ridebook.R;
import com.example.ridebook.ThereProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import models.ModelRides;

public class AdapterRides extends RecyclerView.Adapter<AdapterRides.MyHolder> {

    Context context;
    List<ModelRides> postRides;

    String myUid;

    private DatabaseReference Ridesref;
    private DatabaseReference Postsref;

    public AdapterRides(Context context, List<ModelRides> postRides) {
        this.context = context;
        this.postRides = postRides;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Ridesref = FirebaseDatabase.getInstance().getReference().child("Rides");
        Postsref = FirebaseDatabase.getInstance().getReference().child("Posts");

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                checkAndRemoveExpiredRides();
                handler.postDelayed(this, 60000); // Check every minute
            }
        };
        handler.postDelayed(runnable, 60000);
    }

    private void checkAndRemoveExpiredRides() {

        long currentTimeInMillis = System.currentTimeMillis();

        for (int i = postRides.size() - 1; i >= 0; i--) {
            ModelRides ride = postRides.get(i);
            if (isRideExpired(ride.getDate(), ride.getTime())) {
                postRides.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, postRides.size());
            }
        }
    }

    private boolean isRideExpired(String date, String time) {
        try {
            String dateTimeString = date + " " + time;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault());
            long rideDateTimeInMillis = sdf.parse(dateTimeString).getTime();
            return System.currentTimeMillis() > rideDateTimeInMillis;
        } catch (java.text.ParseException e) {
            e.printStackTrace(); // Log the exception for debugging
            return false;
        }
    }


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_rides, parent, false);
        return new MyHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String uid = postRides.get(position).getUid();
        String uEmail = postRides.get(position).getuEmail();
        String uName = postRides.get(position).getuName();
        String uDP = postRides.get(position).getuDp();
        String pId = postRides.get(position).getpId();
        String meetingplace = postRides.get(position).getMeeting_place();
        String date = postRides.get(position).getDate();
        String time = postRides.get(position).getTime();
        String destination = postRides.get(position).getDestination();
        String cc = postRides.get(position).getCC();
        String pTimestamp = postRides.get(position).getpTime();

        if (pTimestamp != null) {
            try {
                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                calendar.setTimeInMillis(Long.parseLong(pTimestamp));
                String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                holder.rTimeTv.setText(pTime);
            } catch (NumberFormatException e) {
                holder.rTimeTv.setText("Invalid timestamp");
            }
        } else {
            holder.rTimeTv.setText("Timestamp not available");
        }

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.rNameTv.setText(uName);
        holder.rTimeTv.setText(pTime);
        holder.meetingplace.setText(meetingplace);
        holder.date.setText(date);
        holder.time.setText(time);
        holder.destination.setText(destination);
        holder.CC.setText(cc);

        try {
            Picasso.get().load(uDP).placeholder(R.drawable.ic_default_img).into(holder.rPictureIv);
        } catch (Exception e) {
            // Handle Picasso exception
        }

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedDestination = postRides.get(holder.getAdapterPosition()).getDestination();
                showDirections(selectedDestination);
            }
        });
    }

    private void showDirections(String destination) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destination);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            Uri webIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + destination);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webIntentUri);
            context.startActivity(webIntent);
        }
    }

    @Override
    public int getItemCount() {
        return postRides.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageView rPictureIv;
        LinearLayout profileLayout;
        TextView rNameTv, rTimeTv, meetingplace, date, time, destination, CC;

        ImageButton moreBtn;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            rPictureIv = itemView.findViewById(R.id.rPictureIv);
            rNameTv = itemView.findViewById(R.id.rNameTv);
            rTimeTv = itemView.findViewById(R.id.rTimeTv);
            meetingplace = itemView.findViewById(R.id.meetingplace);
            profileLayout = itemView.findViewById(R.id.profileLayout);
            date = itemView.findViewById(R.id.Date);
            time = itemView.findViewById(R.id.Time);
            destination = itemView.findViewById(R.id.Destination);
            CC = itemView.findViewById(R.id.Cc);
        }
    }
}