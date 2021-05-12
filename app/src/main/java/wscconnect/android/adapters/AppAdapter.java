package wscconnect.android.adapters;

import android.os.Handler;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import wscconnect.android.GlideApp;
import wscconnect.android.R;
import wscconnect.android.activities.MainActivity;
import wscconnect.android.fragments.AppsFragment;
import wscconnect.android.models.AppModel;

/**
 * @author Christopher Walz
 * @copyright 2017-2018 Christopher Walz
 * @license GNU General Public License v3.0 <https://opensource.org/licenses/LGPL-3.0>
 */

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.MyViewHolder> {
    private final AppsFragment fragment;
    private final int viewResource;
    private final MainActivity activity;
    private final List<AppModel> appList;

    public AppAdapter(MainActivity activity, AppsFragment fragment, List<AppModel> appList) {
        this(activity, fragment, appList, R.layout.list_app);
    }

    public AppAdapter(MainActivity activity, AppsFragment fragment, List<AppModel> appList, Integer viewResource) {
        this.activity = activity;
        this.appList = appList;
        this.fragment = fragment;
        this.viewResource = viewResource;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(viewResource, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        AppModel app = appList.get(position);

        holder.name.setText(app.getName());

       /*URI uri = null;
        try {
            uri = new URI(app.getUrl());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (uri != null) {
            holder.url.setText(uri.getHost().replace("www.", ""));
        } else {
            holder.url.setText(app.getUrl());
        }*/
        //holder.setIsRecyclable(false);
        if (app.isLogoAccessible()) {
            GlideApp.with(activity).load(app.getLogo()).into(holder.logo);
        } else {
            GlideApp.with(activity).load(R.drawable.logo_off).into(holder.logo);
        }
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView logo;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.list_app_name);
            //url = view.findViewById(R.id.list_app_url);
            logo = view.findViewById(R.id.list_app_logo);

            view.setOnClickListener(view1 -> switchToDetailView());
        }

        private void switchToDetailView() {
            int position = getAdapterPosition();

            // RecyclerView.NO_POSITION is returned, if notifyDataSetChanged() has been called just now
            if (position != RecyclerView.NO_POSITION) {
                final AppModel app = appList.get(position);
                fragment.switchToDetailView(true, false, app);
            } else {
                // wait a short time and try again.
                new Handler().postDelayed(this::switchToDetailView, 200);
            }
        }
    }
}
