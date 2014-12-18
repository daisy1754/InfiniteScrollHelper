package jp.gr.java_conf.daisy.infinite_scroll_helper;

import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Helper class for infinite-scroll like behavior. Note that this class use
 * {@link android.widget.ListView#setOnScrollListener(android.widget.AbsListView.OnScrollListener)}
 * and {@link android.widget.ListView#addFooterView(android.view.View)}.
 */
public class InfiniteScrollHelper {
    public enum LoadingState {
        INITIAL, PARTIALLY_LOADED, LOADING, COMPLETE_LOADED, ERROR
    }

    public interface ListItemFetcher {
        public boolean canFetchMoreItems();
        public void fetchMoreItems();
    }

    private LoadingState mLoadingState = LoadingState.INITIAL;
    private final View mLoadingIndicatorView;
    private final ListView mListView;

    public InfiniteScrollHelper(
            ListView listView, final ListItemFetcher listItemFetcher, View loadingIndicatorView) {
        mListView = listView;
        mLoadingIndicatorView = loadingIndicatorView;
        reset();

        mListView.addFooterView(mLoadingIndicatorView);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                synchronized (this) {
                    if (firstVisibleItem + visibleItemCount == mListView.getCount()
                            && mLoadingState == LoadingState.PARTIALLY_LOADED
                            && listItemFetcher.canFetchMoreItems()) {
                        mLoadingState = LoadingState.LOADING;
                        listItemFetcher.fetchMoreItems();
                        mLoadingIndicatorView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    public void notifyLoadingCompletion(LoadingState state) {
        mLoadingState = state;
        mLoadingIndicatorView.setVisibility(View.GONE);
        if (state == LoadingState.COMPLETE_LOADED) {
            mListView.removeFooterView(mLoadingIndicatorView);
        }
    }

    public void reset() {
        mLoadingState = LoadingState.INITIAL;
        mLoadingIndicatorView.setVisibility(View.GONE);
        mListView.removeFooterView(mLoadingIndicatorView);
        mListView.addFooterView(mLoadingIndicatorView);
    }
}
