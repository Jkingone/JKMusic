package com.jkingone.jkmusic.ui.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.jkingone.jkmusic.ui.base.LazyFragment;
import com.jkingone.jkmusic.viewmodels.ArtistListViewModel;
import com.jkingone.utils.DensityUtils;
import com.jkingone.jkmusic.GlideApp;
import com.jkingone.jkmusic.R;
import com.jkingone.jkmusic.api.ArtistApi;
import com.jkingone.jkmusic.entity.ArtistList;
import com.jkingone.jkmusic.ui.activity.ArtistListActivity;
import com.jkingone.ui.ContentLoadView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ArtistListFragment extends LazyFragment {

    private static final String TAG = "ArtistListFragment";

    public static ArtistListFragment newInstance(String... params) {
        ArtistListFragment fragment = new ArtistListFragment();
        fragment.setArguments(setParams(params));
        return fragment;
    }

    public static final String ARTIST_AREA = "area";
    public static final String ARTIST_SEX = "sex";

    private static String[] sTitles = {
            "华语男歌手", "华语女歌手", "华语组合",
            "欧美男歌手", "欧美女歌手", "欧美组合",
            "日本男歌手", "日本女歌手", "日本组合",
            "棒子男歌手", "棒子女歌手", "棒子组合",
            "其他男歌手", "其他女歌手", "其他组合",
    };

    private static Integer[] sAreas = {
            ArtistApi.AREA_CHINA, ArtistApi.AREA_CHINA, ArtistApi.AREA_CHINA,
            ArtistApi.AREA_EU, ArtistApi.AREA_EU, ArtistApi.AREA_EU,
            ArtistApi.AREA_JAPAN, ArtistApi.AREA_JAPAN, ArtistApi.AREA_JAPAN,
            ArtistApi.AREA_KOREA, ArtistApi.AREA_KOREA, ArtistApi.AREA_KOREA,
            ArtistApi.AREA_OTHER, ArtistApi.AREA_OTHER, ArtistApi.AREA_OTHER
    };

    private static int[] sSexes = {
            ArtistApi.SEX_MALE, ArtistApi.SEX_FEMALE, ArtistApi.SEX_GROUP,
            ArtistApi.SEX_MALE, ArtistApi.SEX_FEMALE, ArtistApi.SEX_GROUP,
            ArtistApi.SEX_MALE, ArtistApi.SEX_FEMALE, ArtistApi.SEX_GROUP,
            ArtistApi.SEX_MALE, ArtistApi.SEX_FEMALE, ArtistApi.SEX_GROUP,
            ArtistApi.SEX_MALE, ArtistApi.SEX_FEMALE, ArtistApi.SEX_GROUP,
    };

    @BindView(R.id.recycle_common)
    RecyclerView mRecyclerViewContent;
    @BindView(R.id.content_common)
    ContentLoadView mContentLoadView;

    private Unbinder mUnbinder;

    private HotAdapter mHotAdapter;
    private ContentAdapter mContentAdapter;

    private ArtistListViewModel mArtistListViewModel;
    private Observer<PagedList<ArtistList>> mArtistListObserver = artistLists -> {
        mHotAdapter.submitList(artistLists);
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArtistListViewModel = ViewModelProviders.of(this).get(ArtistListViewModel.class);
        mArtistListViewModel.setParams(ArtistApi.AREA_ALL, ArtistApi.SEX_NONE, ArtistApi.ORDER_HOT, null);
    }

    @Override
    protected boolean onLazyLoadOnce() {
        mArtistListViewModel.getArtistListLiveData().observe(this, mArtistListObserver);
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.common_root_none, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mRecyclerViewContent.setBackgroundColor(getResources().getColor(R.color.gray_trans));
        mRecyclerViewContent.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerViewContent.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int pos = parent.getChildAdapterPosition(view);
                if (pos % 3 == 0) {
                    outRect.bottom = DensityUtils.dp2px(parent.getContext(), 8);
                }
            }
        });

        if (mContentAdapter == null) {
            mContentAdapter = new ContentAdapter(getContext());
        }

        mRecyclerViewContent.setAdapter(mContentAdapter);
        mContentLoadView.postLoadComplete();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mArtistListViewModel.getArtistListLiveData().removeObserver(mArtistListObserver);
    }

    class ContentAdapter extends RecyclerView.Adapter {

        private static final int TYPE_HEAD = 1;
        private static final int TYPE_CONTENT = 2;

        private Context mContext;

        ContentAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEAD;
            }
            return TYPE_CONTENT;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_HEAD:
                    return new HeadViewHolder(LayoutInflater.from(mContext)
                            .inflate(R.layout.item_list_artist_head, parent, false));

                case TYPE_CONTENT:
                    return new ContentViewHolder(LayoutInflater.from(mContext)
                            .inflate(R.layout.item_list_artist, parent, false));

                default:
                    throw new IllegalArgumentException("no type");
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ContentViewHolder) {
                ContentViewHolder contentViewHolder = (ContentViewHolder) holder;
                contentViewHolder.mTextView.setText(sTitles[position - 1]);
                contentViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, ArtistListActivity.class);
                        intent.putExtra(ARTIST_AREA, sAreas[position - 1]);
                        intent.putExtra(ARTIST_SEX, sSexes[position - 1]);
                        ArtistListFragment.this.startActivity(intent);
                    }
                });
            } else if (holder instanceof HeadViewHolder) {
                HeadViewHolder headViewHolder = (HeadViewHolder) holder;

                if (mHotAdapter == null) {
                    mHotAdapter = new HotAdapter(getContext());
                }
                headViewHolder.mRecyclerView.setAdapter(mHotAdapter);

                headViewHolder.mTextView.setOnClickListener(v -> {
                    Intent intent = new Intent(mContext, ArtistListActivity.class);
                    intent.putExtra(ARTIST_AREA, ArtistApi.AREA_ALL);
                    intent.putExtra(ARTIST_SEX, ArtistApi.SEX_NONE);
                    ArtistListFragment.this.startActivity(intent);
                });

            }
        }

        @Override
        public int getItemCount() {
            return sTitles.length + 1;
        }

        class ContentViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tv_item)
            TextView mTextView;

            ContentViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        class HeadViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.tv_hot)
            TextView mTextView;
            @BindView(R.id.recycle_hot)
            RecyclerView mRecyclerView;

            HeadViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext,
                        LinearLayoutManager.HORIZONTAL, false));
            }
        }
    }

    class HotAdapter extends PagedListAdapter<ArtistList, HotAdapter.HotViewHolder> {

        private int h;
        private int w;

        private Context mContext;

        HotAdapter(Context context) {
            super(new DiffUtil.ItemCallback<ArtistList>() {
                @Override
                public boolean areItemsTheSame(@NonNull ArtistList artistList, @NonNull ArtistList newArtist) {
                    return artistList.artistId.equals(newArtist.artistId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull ArtistList artistList, @NonNull ArtistList newArtist) {
                    return artistList.equals(newArtist);
                }
            });
            mContext = context;
            w = DensityUtils.dp2px(context, 96 + 8);
            h = DensityUtils.dp2px(mContext, 8 + 8) + DensityUtils.sp2px(mContext, 14);
        }

        @NonNull
        @Override
        public HotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new HotViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.item_universal, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull HotViewHolder holder, int position) {
            ArtistList artistList = getItem(position);
            if (artistList != null) {
                holder.mTextView.setText(artistList.name);

                GlideApp.with(ArtistListFragment.this)
                        .asBitmap()
                        .override(w)
                        .load(artistList.avatarBig)
                        .transition(BitmapTransitionOptions.withCrossFade())
                        .transform(new RoundedCorners(8))
                        .into(new BitmapImageViewTarget(holder.mImageView));
            }

        }

        class HotViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.iv_item)
            ImageView mImageView;
            @BindView(R.id.tv_item)
            TextView mTextView;

            HotViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                mTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, h));
                mTextView.setGravity(Gravity.CENTER);
                mTextView.setSingleLine(true);
                itemView.setLayoutParams(new LinearLayout.LayoutParams(w, w + h));
            }
        }
    }
}
