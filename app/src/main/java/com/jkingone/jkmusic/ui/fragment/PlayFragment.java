package com.jkingone.jkmusic.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jkingone.commonlib.Utils.DensityUtils;
import com.jkingone.jkmusic.Constant;
import com.jkingone.jkmusic.MusicBroadcastReceiver;
import com.jkingone.jkmusic.R;
import com.jkingone.jkmusic.data.entity.SongInfo;
import com.jkingone.jkmusic.data.local.ContentHelper;
import com.jkingone.jkmusic.ui.activity.BaseActivity;
import com.jkingone.jkmusic.ui.activity.PlayActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class PlayFragment extends LazyFragment {

    private static final String TAG = "PlayFragment";

    public static final String CUR_SONG = "cur_song";
    public static final String CUR_INDEX = "cur_index";

    @BindView(R.id.pager)
    ViewPager mViewPager;
    @BindView(R.id.iv_play)
    ImageView mImageViewPlay;
    @BindView(R.id.iv_menu)
    ImageView mImageViewMenu;

    private List<View> mRootViews = new ArrayList<>(6);
    private ImageView mImageViewCover;
    private TextView mTextViewSinger;
    private TextView mTextViewSongName;
    private List<SongInfo> mSongInfos = new ArrayList<>();

    private Unbinder mUnbinder;

    private SongInfo mCurSongInfo;
    private int mCurIndex = Integer.MIN_VALUE;

    private boolean isComplete;
    private boolean isNext;

    private BaseActivity mBaseActivity;

    public static PlayFragment newInstance(String... params) {
        PlayFragment fragment = new PlayFragment();
        fragment.setArguments(setParams(params));
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            mBaseActivity = (BaseActivity) context;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mSongInfos = new ContentHelper(getContext()).getMusic();

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            int lastOffset = 0;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                isNext = positionOffsetPixels > lastOffset;
                lastOffset = positionOffsetPixels;
            }

            @Override
            public void onPageSelected(int position) {
                if (!isComplete) {
                    if (isNext) {
                        mBaseActivity.checkMusicManagerService().next();
                    } else {
                        mBaseActivity.checkMusicManagerService().previous();
                    }
                    Log.i(TAG, "onPageSelected: " + isNext);
                } else {
                    updateViewPager(position);
                    Log.i(TAG, "onPageSelected: ");
                }
                isComplete = false;
                if (position == 0) {
                    mViewPager.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isComplete = true;
                            mViewPager.setCurrentItem(Constant.PAGER_SIZE - 2, false);
                        }
                    }, 500);
                }
                if (position == Constant.PAGER_SIZE - 1) {
                    mViewPager.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isComplete = true;
                            mViewPager.setCurrentItem(1, false);
                        }
                    }, 500);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mBaseActivity.getMusicManagerService().setPlayCallback(new MusicBroadcastReceiver.PlayCallback() {
            @Override
            public void playStateChange(boolean isPlaying) {
                if (isPlaying) {
                    mImageViewPlay.setImageResource(R.drawable.music_xxh_yellow);
                } else {
                    mImageViewPlay.setImageResource(R.drawable.music);
                }
            }

            @Override
            public void mediaSourceChange(boolean indexChanged, int index, List<SongInfo> songInfos) {

            }

            @Override
            public void indexChanged(int index, boolean isComplete) {
                updateIndex(index);
                PlayFragment.this.isComplete = isComplete;
                if (isComplete) {
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
                } else {
                    updateViewPager(mViewPager.getCurrentItem());
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @OnClick(R.id.iv_menu)
    public void onMenuClick() {
        if (mBaseActivity.checkMusicManagerService() != null) {
            mBaseActivity.checkMusicManagerService().prepareMediaSources(mSongInfos);
            mBaseActivity.checkMusicManagerService().playIndex(2);

            updateIndex(2);

            isComplete = true;
            mViewPager.setAdapter(new MusicAdapter());
            mViewPager.setCurrentItem(2);
        }
    }

    @OnClick(R.id.iv_play)
    public void onPlayClick() {
        if (mBaseActivity.checkMusicManagerService() != null) {
            if (mBaseActivity.checkMusicManagerService().isPlaying()) {
                mBaseActivity.checkMusicManagerService().pause();
            } else {
                mBaseActivity.checkMusicManagerService().play();
            }
        }
    }

    @OnClick(R.id.pager)
    public void onPagerClick() {
        Intent intent = new Intent(mBaseActivity, PlayActivity.class);
        intent.putExtra(CUR_SONG, mCurSongInfo);
        intent.putExtra(CUR_INDEX, mCurIndex);
        startActivity(intent);
    }

    private class MusicAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return Constant.PAGER_SIZE;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return object == view;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = instantiateView(container, position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            if (position < mRootViews.size()) {
                container.removeView(mRootViews.get(position));
                mRootViews.set(position, null);
            }
        }
    }

    private synchronized View instantiateView(ViewGroup container, int position) {
        int size = mRootViews.size();
        if (position >= size) {
            for (int i = size; i <= position; i++) {
                mRootViews.add(null);
            }
        }
        if (mRootViews.get(position) == null) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.pager_bottom_music, container, false);
            mRootViews.set(position, view);
        }
        return mRootViews.get(position);
    }

    private void updateIndex(int index) {
        mCurIndex = index;
        mCurSongInfo = mSongInfos.get(mCurIndex);
    }

    private void updateViewPager(int pos) {
        View view = instantiateView(mViewPager, pos);
        mImageViewCover = view.findViewById(R.id.iv_cover);
        mTextViewSinger = view.findViewById(R.id.tv_singer);
        mTextViewSongName = view.findViewById(R.id.tv_songName);
        Picasso.get()
                .load(mCurSongInfo.getPicUrl())
                .centerCrop()
                .resize(DensityUtils.dp2px(mBaseActivity, 48), DensityUtils.dp2px(mBaseActivity, 48))
                .placeholder(R.drawable.music)
                .into(mImageViewCover);
        mTextViewSinger.setText(mCurSongInfo.getArtist());
        mTextViewSongName.setText(mCurSongInfo.getTitle());
    }
}
