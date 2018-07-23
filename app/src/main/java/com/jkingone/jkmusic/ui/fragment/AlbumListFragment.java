package com.jkingone.jkmusic.ui.fragment;


import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jkingone.common.Utils.DensityUtils;
import com.jkingone.jkmusic.R;
import com.jkingone.jkmusic.Utils;
import com.jkingone.jkmusic.entity.AlbumList;
import com.jkingone.jkmusic.ui.base.BaseFragment;
import com.jkingone.jkmusic.ui.mvp.AlbumListPresenter;
import com.jkingone.jkmusic.ui.mvp.contract.AlbumListContract;
import com.jkingone.ui.customview.ContentLoadView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumListFragment extends BaseFragment<AlbumListPresenter> implements AlbumListContract.ViewCallback {

    public static AlbumListFragment newInstance(String... params) {
        AlbumListFragment fragment = new AlbumListFragment();
        fragment.setArguments(setParams(params));
        return fragment;
    }

    @BindView(R.id.recycle_universal)
    RecyclerView mRecyclerView;
    @BindView(R.id.content_universal)
    ContentLoadView mContentLoadView;

    private List<AlbumList> mAlbumLists = new ArrayList<>();
    private AlbumAdapter mAlbumAdapter;

    private int offset = 0;
    private static final int LIMIT = 30;

    private static final String TAG = "AlbumListFragment";

    @Override
    protected void onLazyLoadOnce() {
        mPresenter.getAlbumList(offset, LIMIT);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_load_universal_notoolbar, container, false);
        ButterKnife.bind(this, view);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        manager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.bottom = 6;
                outRect.left = 6;
                outRect.right = 6;
                outRect.top = 6;
            }
        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    Picasso.get().resumeTag(TAG);

                    int[] pos = new int[manager.getSpanCount()];
                    manager.findLastVisibleItemPositions(pos);
                    int max = pos[0];
                    for (int value : pos) {
                        if (value > max) {
                            max = value;
                        }
                    }
                    if (max == manager.getItemCount() - 1 && manager.getChildCount() > 0) {
                        offset += LIMIT;
                        mPresenter.getAlbumList(offset, LIMIT);
                    }
                } else {
                    Picasso.get().pauseTag(TAG);
                }
            }
        });
        mContentLoadView.setLoadRetryListener(new ContentLoadView.LoadRetryListener() {
            @Override
            public void onRetry() {
                mContentLoadView.postLoading();
                mPresenter.getAlbumList(offset, LIMIT);
            }
        });
        return view;
    }

    @Override
    public AlbumListPresenter createPresenter() {
        return new AlbumListPresenter(this);
    }

    @Override
    public void showAlbumList(List<AlbumList> albumLists) {
        if (albumLists == null) {
            mContentLoadView.postLoadFail();
            return;
        }

        mContentLoadView.postLoadComplete();

        mAlbumLists.addAll(albumLists);
        if (mAlbumAdapter == null) {
            mAlbumAdapter = new AlbumAdapter(getContext());
            mRecyclerView.setAdapter(mAlbumAdapter);
        } else {
            mAlbumAdapter.notifyDataSetChanged();
        }
    }

    class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.VH> {

        private Context mContext;
        private RecyclerView.LayoutParams mLayoutParams;

        AlbumAdapter(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(mContext).inflate(R.layout.item_list_albumlist, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            AlbumList albumList = mAlbumLists.get(position);
            if (position == 0 || position == 2) {
                mLayoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, DensityUtils.dp2px(mContext, 140));
            } else {
                mLayoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, DensityUtils.dp2px(mContext, 120));
            }
            holder.itemView.setLayoutParams(mLayoutParams);
            if (Utils.checkStringNotNull(albumList.getPicBig())) {
                Picasso.get().
                        load(albumList.getPicBig())
                        .resize(DensityUtils.dp2px(mContext, 120), DensityUtils.dp2px(mContext, 120))
                        .centerCrop()
                        .tag(TAG)
                        .into(holder.mImageView);
            }
            holder.mTextView.setText(albumList.getPublishTime());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return mAlbumLists.size();
        }

        class VH extends RecyclerView.ViewHolder {

            @BindView(R.id.iv_album)
            ImageView mImageView;
            @BindView(R.id.tv_publish)
            TextView mTextView;

            VH(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}