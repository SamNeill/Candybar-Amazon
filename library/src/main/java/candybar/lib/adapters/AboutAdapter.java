package candybar.lib.adapters;

import static candybar.lib.helpers.DrawableHelper.getDrawableId;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;

import candybar.lib.BuildConfig;
import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.fragments.dialog.CreditsFragment;
import candybar.lib.fragments.dialog.LicensesFragment;
import candybar.lib.helpers.ConfigurationHelper;
import candybar.lib.preferences.Preferences;
import candybar.lib.utils.CandyBarGlideModule;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-2016 Dani Mahardhika
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class AboutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;

    private int mItemCount;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_FOOTER = 2;
    private static final int TYPE_BOTTOM_SHADOW = 3;

    public AboutAdapter(@NonNull Context context, int spanCount) {
        mContext = context;

        mItemCount = 2;
        boolean cardMode = (spanCount > 1);
        if (!cardMode) {
            mItemCount += 1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.fragment_about_item_header, parent, false);
            return new HeaderViewHolder(view);
        }

        View view = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_settings_item_footer, parent, false);
        return new ShadowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;

            String imageUri = mContext.getString(R.string.about_image);

            if (ColorHelper.isValidColor(imageUri)) {
                headerViewHolder.image.setBackgroundColor(Color.parseColor(imageUri));
            } else {
                if (!URLUtil.isValidUrl(imageUri)) {
                    imageUri = "drawable://" + getDrawableId(imageUri);
                }

                if (CandyBarGlideModule.isValidContextForGlide(mContext)) {
                    Glide.with(mContext)
                            .load(imageUri)
                            .transition(DrawableTransitionOptions.withCrossFade(300))
                            .skipMemoryCache(true)
                            .diskCacheStrategy(imageUri.contains("drawable://")
                                    ? DiskCacheStrategy.NONE
                                    : DiskCacheStrategy.RESOURCE)
                            .into(headerViewHolder.image);
                }
            }

            String profileUri = mContext.getResources().getString(R.string.about_profile_image);
            if (!URLUtil.isValidUrl(profileUri)) {
                profileUri = "drawable://" + getDrawableId(profileUri);
            }

            if (CandyBarGlideModule.isValidContextForGlide(mContext)) {
                Glide.with(mContext)
                        .load(profileUri)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(profileUri.contains("drawable://")
                                ? DiskCacheStrategy.NONE
                                : DiskCacheStrategy.RESOURCE)
                        .into(headerViewHolder.profile);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HEADER;
        if (position == 1) {
            return TYPE_FOOTER;
        }
        return TYPE_BOTTOM_SHADOW;
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final ImageView image;
        private final ImageView profile;

        HeaderViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            profile = itemView.findViewById(R.id.profile);
            TextView subtitle = itemView.findViewById(R.id.subtitle);
            RecyclerView recyclerView = itemView.findViewById(R.id.recyclerview);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, true));
            recyclerView.setHasFixedSize(true);

            int topMargin = mContext.getResources().getDimensionPixelSize(R.dimen.content_margin) * 4;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) recyclerView.getLayoutParams();
            params.topMargin = topMargin;
            recyclerView.setLayoutParams(params);

            String[] urls = mContext.getResources().getStringArray(R.array.about_social_links);
            if (urls.length == 0) {
                recyclerView.setVisibility(View.GONE);

                subtitle.setPadding(
                        subtitle.getPaddingLeft(),
                        subtitle.getPaddingTop(),
                        subtitle.getPaddingRight(),
                        subtitle.getPaddingBottom() + mContext.getResources().getDimensionPixelSize(R.dimen.content_margin));
            } else {
                if (recyclerView.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                    params = (LinearLayout.LayoutParams) recyclerView.getLayoutParams();
                    if (urls.length < 7) {
                        params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                        params.gravity = Gravity.CENTER_HORIZONTAL;
                        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                    }
                    params.topMargin = topMargin;
                    recyclerView.setLayoutParams(params);
                }
                recyclerView.setAdapter(new AboutSocialAdapter(mContext, urls));
            }

            MaterialCardView card = itemView.findViewById(R.id.card);
            if (card != null) {
                card.setCardBackgroundColor(ColorHelper.getAttributeColor(mContext, android.R.attr.colorBackground));
                card.setStrokeColor(ColorHelper.getAttributeColor(mContext, R.attr.cb_cardStroke));
                
                if (CandyBarApplication.getConfiguration().getAboutStyle() == CandyBarApplication.Style.PORTRAIT_FLAT_LANDSCAPE_FLAT &&
                        card != null) {
                    if (card.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                        card.setRadius(0f);
                        card.setUseCompatPadding(false);
                        int margin = mContext.getResources().getDimensionPixelSize(R.dimen.card_margin);
                        StaggeredGridLayoutManager.LayoutParams cardParams = (StaggeredGridLayoutManager.LayoutParams) card.getLayoutParams();
                        cardParams.setMargins(0, 0, margin, margin);
                        cardParams.setMarginEnd(margin);
                    }
                }

                if (mContext.getResources().getBoolean(R.bool.use_flat_card)) {
                    card.setStrokeWidth(mContext.getResources().getDimensionPixelSize(R.dimen.card_stroke_width));
                    card.setCardElevation(0);
                    card.setUseCompatPadding(false);
                }

                if (!Preferences.get(mContext).isCardShadowEnabled()) {
                    if (card != null) card.setCardElevation(0);
                    profile.setElevation(0);
                }
            }

            subtitle.setText(HtmlCompat.fromHtml(
                    mContext.getResources().getString(R.string.about_desc), HtmlCompat.FROM_HTML_MODE_COMPACT));
            subtitle.setMovementMethod(LinkMovementMethod.getInstance());
            subtitle.setTextColor(ColorHelper.getAttributeColor(mContext, android.R.attr.textColorSecondary));
        }
    }

    private class ShadowViewHolder extends RecyclerView.ViewHolder {

        ShadowViewHolder(View itemView) {
            super(itemView);
            if (!Preferences.get(mContext).isCardShadowEnabled()) {
                View shadow = itemView.findViewById(R.id.shadow);
                shadow.setVisibility(View.GONE);
            }
        }
    }
}
