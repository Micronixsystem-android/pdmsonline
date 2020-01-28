package com.fleetshipdigitalboard.adapter;

import android.content.Context;

import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.fleetshipdigitalboard.R;
import com.fleetshipdigitalboard.model.SliderResponse;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter {

    private ArrayList<SliderResponse> f;
    private Context context;

    public ViewPagerAdapter(ArrayList<SliderResponse> f, Context context) {
        this.f = f;
        this.context = context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.slider_layout_image, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.image);

        Glide.with(context).load(f.get(position).getFilename()).override(Target.SIZE_ORIGINAL)
                .into(imageView);
        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

    @Override
    public int getCount() {
        return f.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    private String getExt(String filePath) {
        int strLength = filePath.lastIndexOf(".");
        if (strLength > 0)
            return filePath.substring(strLength + 1).toLowerCase();
        return null;
    }
}
