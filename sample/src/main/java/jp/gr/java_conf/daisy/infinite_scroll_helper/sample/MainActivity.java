package jp.gr.java_conf.daisy.infinite_scroll_helper.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.gr.java_conf.daisy.infinite_scroll_helper.InfiniteScrollHelper;

public class MainActivity extends Activity {

    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private InfiniteScrollHelper mInfiniteScrollHelper;
    private Handler mHandler;
    private Runnable mLoadNextPageRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new ArrayAdapter<String>(this, R.layout.list_item);
        mListView.setAdapter(mAdapter);
        final FakeListItemFetcher fetcher = new FakeListItemFetcher();
        mInfiniteScrollHelper = new InfiniteScrollHelper(
                mListView, fetcher, new ProgressBar(this));
        mAdapter.addAll(fetcher.initialItems());
        mInfiniteScrollHelper.notifyLoadingCompletion(
                InfiniteScrollHelper.LoadingState.PARTIALLY_LOADED);
        mHandler = new Handler();
        findViewById(R.id.resetButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInfiniteScrollHelper.reset();
                if (mLoadNextPageRunnable != null) {
                    mHandler.removeCallbacks(mLoadNextPageRunnable);
                }
                mListView.smoothScrollToPosition(0);
                mAdapter.clear();
                mAdapter.addAll(fetcher.initialItems());
                mInfiniteScrollHelper.notifyLoadingCompletion(
                        InfiniteScrollHelper.LoadingState.PARTIALLY_LOADED);
            }
        });
    }

    private class FakeListItemFetcher implements InfiniteScrollHelper.ListItemFetcher {
        private final int PAGE_SIZE = 10;
        private final List<String> FAKE_DATA = new ArrayList<String>(Arrays.asList(new String[] {
                "Cat", "Dog", "Lion", "Elephant", "Monkey", "Sheep", "Tiger", "Zebra", "Giraffe",
                "Cow", "Horse", "Human", "Rhino", "Bear", "Deer", "Chimpanzee", "Hedgehog",
                "Mouse", "Rabbit", "Turtle", "Boar", "Panda", "Bird", "Fox", "Wolf"
        }));
        private int loadedDataIndex;

        private List<String> initialItems() {
            loadedDataIndex = PAGE_SIZE;
            return FAKE_DATA.subList(0, loadedDataIndex);
        }

        @Override
        public boolean canFetchMoreItems() {
            return loadedDataIndex < FAKE_DATA.size() - 1;
        }

        @Override
        public void fetchMoreItems() {
            mLoadNextPageRunnable = new Runnable() {
                @Override
                public void run() {
                    int nextIndex = loadedDataIndex + PAGE_SIZE;
                    if (nextIndex > FAKE_DATA.size()) {
                        nextIndex = FAKE_DATA.size();
                    }
                    mAdapter.addAll(FAKE_DATA.subList(loadedDataIndex, nextIndex));
                    loadedDataIndex = nextIndex;
                    mInfiniteScrollHelper.notifyLoadingCompletion(
                            loadedDataIndex == FAKE_DATA.size()
                                    ? InfiniteScrollHelper.LoadingState.COMPLETE_LOADED
                                    : InfiniteScrollHelper.LoadingState.PARTIALLY_LOADED
                    );
                }
            };
            // Simulate network delay.
            mHandler.postDelayed(mLoadNextPageRunnable, 1000);
        }
    }
}
