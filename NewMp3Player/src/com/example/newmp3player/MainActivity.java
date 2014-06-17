package com.example.newmp3player;

import java.util.ArrayList;
import java.util.List;

import com.example.dialog.NewPlaylistDialog;
import com.example.dialog.YesOrNoDialog;
import com.example.sqlite.DProvider;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.widget.Spinner;

@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity implements
		NewPlaylistDialog.DialogListener ,LocalActivity.LocalFragmentListener,YesOrNoDialog.YesOrNoDialogListener{
	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 初始化全部播放列表
		InitSplite init = new InitSplite();
		init.start();
		mViewPager = (ViewPager) findViewById(R.id.pager);

		final ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// 不显示标题
		bar.setDisplayShowTitleEnabled(false);
		bar.setDisplayUseLogoEnabled(false);
		// bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

		mTabsAdapter = new TabsAdapter(this, mViewPager);
		mTabsAdapter.addTab(bar.newTab().setText(getString(R.string.TabPlay))
				.setIcon(R.drawable.ic_tab_songs_unselected),
				TabPlayFragment.class, null);
		mTabsAdapter.addTab(
				bar.newTab().setText(getString(R.string.TabPlayList))
						.setIcon(R.drawable.ic_tab_playlists_unselected),
				LocalActivity.class, null);
		mTabsAdapter.addTab(bar.newTab().setText(getString(R.string.TabDown))
				.setIcon(R.drawable.ic_menu_party_shuffle),
				RemoteActivity.class, null);

		if (savedInstanceState != null) {
			bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
		}
	}

	
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		//退出
        if ((Intent.FLAG_ACTIVITY_CLEAR_TOP & intent.getFlags()) != 0) {
        	Log.i("","onNewIntent is finish");
               finish();
        }

	}
	


	private class InitSplite extends Thread{

		@Override
		public void run() {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				DProvider dprovider = DProvider.getInstance(getApplicationContext());
				dprovider.InitDate();
			}else{
				Log.i("", "存储卡没有准备好");
			}
		}
		
	} 

	// 针对不同的tab页面，设置不同的菜单按钮
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		int fag = mViewPager.getCurrentItem();
		if (fag == 2) {
			menu.clear();
			menu.add(0, 1, 0, getString(R.string.tab111_menu_1));
			menu.add(0, 2, 0, getString(R.string.tab111_menu_2));
		} else if (fag == 1) {
			menu.clear();
			menu.add(0, 1, 0, getString(R.string.tab222_menu_1));
			menu.add(0, 2, 0, getString(R.string.tab222_menu_2));
		}else if (fag == 0){
			menu.clear();
			menu.add(0, 1, 0, getString(R.string.tab000_menu));
			menu.add(0, 2, 0, getString(R.string.tab_set));
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
	}

	/**
	 * This is a helper class that implements the management of tabs and all
	 * details of connecting a ViewPager with associated TabHost. It relies on a
	 * trick. Normally a tab host has a simple API for supplying a View or
	 * Intent that each tab will show. This is not sufficient for switching
	 * between pages. So instead we make the content part of the tab host 0dp
	 * high (it is not shown) and the TabsAdapter supplies its own dummy view to
	 * show as the tab content. It listens to changes in tabs, and takes care of
	 * switch to the correct paged in the ViewPager whenever the selected tab
	 * changes.
	 */
	public static class TabsAdapter extends FragmentPagerAdapter implements
			ActionBar.TabListener, ViewPager.OnPageChangeListener {
		private final Context mContext;
		private final ActionBar mActionBar;
		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

		static final class TabInfo {
			private final Class<?> clss;
			private final Bundle args;

			TabInfo(Class<?> _class, Bundle _args) {
				clss = _class;
				args = _args;
			}
		}

		public TabsAdapter(FragmentActivity activity, ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			mActionBar = activity.getActionBar();
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
			TabInfo info = new TabInfo(clss, args);
			tab.setTag(info);
			tab.setTabListener(this);
			mTabs.add(info);
			mActionBar.addTab(tab);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {
			TabInfo info = mTabs.get(position);
			return Fragment.instantiate(mContext, info.clss.getName(),
					info.args);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			mActionBar.setSelectedNavigationItem(position);
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			Object tag = tab.getTag();
			for (int i = 0; i < mTabs.size(); i++) {
				if (mTabs.get(i) == tag) {
					mViewPager.setCurrentItem(i);
				}
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}
	}

	
	@Override
	public void onArticleSelected(int index, Bundle bundle) {
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		for (int i = 0; i < fragments.size(); i++) {
			//如果index是0，表示是newplaylistdialog
			if (index == 0 && fragments.get(i).getClass().toString()
					.equals("class com.example.newmp3player.LocalActivity")) {
				Spinner spinner = (Spinner) fragments.get(i).getActivity()
						.findViewById(R.id.spinner1);
				spinner.setSelection(0);
				break;
			}
		}
	}

	@Override
	public void onMp3Selected(Bundle bundle) {
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		for (int i = 0; i < fragments.size(); i++) {
			if(fragments.get(i).getClass().toString()
					.equals("class com.example.newmp3player.TabPlayFragment")){
				TabPlayFragment fragment = (TabPlayFragment)fragments.get(i);
				fragment.SetMp3Info(bundle);
				break;
			}
		}
	}



	@Override
	public void onItemSelected() {
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		for (int i = 0; i < fragments.size(); i++) {
			//如果index是0，表示是newplaylistdialog
			if (fragments.get(i).getClass().toString()
					.equals("class com.example.newmp3player.LocalActivity")) {
				LocalActivity fragment = (LocalActivity)fragments.get(i);
				fragment.updateList();
				break;
			}
		}
	}

}