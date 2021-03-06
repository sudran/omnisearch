package com.sudran.omnisearch.android.framework;

import java.lang.Thread.State;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.sudran.appman.R;
import com.sudran.omnisearch.android.app.ApplicationSearchProvider;
import com.sudran.omnisearch.android.views.ObservableScrollView;
import com.sudran.omnisearch.android.views.ObservableScrollView.OnScrollChangedListener;

public class OmniSearchHome extends Activity {

	private static final String SEARCH_STRING_KEY = "app_state:search_box:search_string";
	private static final String SEARCH_BOX_SELECTION_START_KEY = "app_state:search_box:selection_start";
	private static final String SEARCH_BOX_SELECTION_END_KEY = "app_state:search_box:selection_end";

	static Pattern WHITE_SPACE_PATTERN = Pattern.compile("\\s+");

	private int searchResultsCount;

	private TextView searchCountText;

	private EditText searchBox;
	
//	private StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
	
	private List<SearchThread<? extends BaseSearchProvider<? extends ISearchableElement>>> searchThreads;
	private LinearLayout searchResultsView;

	//private ReentrantLock appLoaderLock;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_omnisearch_home);
		restoreFromSavedState(savedInstanceState);
		initContent();
		searchResultsView = (LinearLayout) findViewById(R.id.appList);
		searchCountText = (TextView) findViewById(R.id.searchCount);
		displayAppList_fork();
		addListeners_fork();
		ObservableScrollView scrollView = (ObservableScrollView) findViewById(R.id.appListScrollView);
		scrollView.addOnScrollChangedListener(new OnScrollChangedListener() {

			@Override
			public boolean onScrollChanged(ObservableScrollView observableScrollView,
					int l, int t, int oldl, int oldt) {
				EditText textView = getSearchBox();
				InputMethodManager imm = 
						(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
				return true;
			}
		});
	}

	private void initContent() {
		searchThreads = new LinkedList<SearchThread<? extends BaseSearchProvider
				<? extends ISearchableElement>>>();
		
		SearchThread<ApplicationSearchProvider> appSearchThread = 
				new SearchThread<ApplicationSearchProvider>(this, 
						new ApplicationSearchProvider(getPackageManager(),this));
		searchThreads.add(appSearchThread);
	}

	private void displayAppList_fork() {
		Thread displayAppList = new Thread(){
			public void run() {
				//    			try {
				//					sleep(1000);
				//				} catch (InterruptedException e) {
				//
				//				}
				displayAppList();
			};
		};
		displayAppList.start();
	}

	private void displayAppList() {
		for (SearchThread<? extends BaseSearchProvider<? extends ISearchableElement>> searchThread 
				: searchThreads) {
			searchThread.getSearchProvider().load();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		EditText searchBox = getSearchBox();
		outState.putCharSequence(SEARCH_STRING_KEY, searchBox.getText());
		outState.putInt(SEARCH_BOX_SELECTION_START_KEY, searchBox.getSelectionStart());
		outState.putInt(SEARCH_BOX_SELECTION_END_KEY, searchBox.getSelectionEnd());
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		restoreFromSavedState(savedInstanceState);
	}

	private void restoreFromSavedState(Bundle savedInstanceState) {
		if(savedInstanceState == null)
			return;
		CharSequence charSequence = savedInstanceState.getCharSequence(SEARCH_STRING_KEY);
		getSearchBox().setText(charSequence);
		int selectionStart = savedInstanceState.getInt(SEARCH_BOX_SELECTION_START_KEY);
		int selectionEnd = savedInstanceState.getInt(SEARCH_BOX_SELECTION_END_KEY);
		if(selectionStart != -1 && selectionEnd != -1)
			getSearchBox().setSelection(selectionStart, selectionEnd);
	}

	//	private void displayAppList() {
	//	long currentTimeMillis = System.currentTimeMillis();
	//		final PackageManager pm = getPackageManager();
	//    	//get a list of installed apps.
	//	long currentTimeMillis2 = System.currentTimeMillis();
	//    	List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
	//    System.err.println("getAppList: " + (System.currentTimeMillis() - currentTimeMillis2));
	//    	final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.appList);
	//    	for (final ApplicationInfo packageInfo : packages) {
	//    		final CharSequence applicationLabel = pm.getApplicationLabel(packageInfo) + "\n";
	//    		final LayoutParams appDetailsLayoutParams = 
//					new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	//    		final LayoutParams appNameLayoutParams = 
//					new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
	//    		final LinearLayout appDetailsLinearLayout = new LinearLayout(this);
	//    		final TextView textView = new TextView(this);
	//    		textView.setText(applicationLabel);
	//    		textView.setGravity(Gravity.CENTER_VERTICAL);
	//    		Drawable drawable = packageInfo.loadIcon(pm);
	//    		final ImageView appIcon = new ImageView(this);
	//    		appIcon.setImageDrawable(drawable);
	//    		int side = (int) getResources().getDimension(R.dimen.preferred_app_detail_height);
	//    		final LayoutParams appIconLayoutParams = new LayoutParams(side, side);
	//    		runOnUiThread(new Runnable() {
	//				@Override
	//				public void run() {
	//					linearLayout.addView(appDetailsLinearLayout, appDetailsLayoutParams);
	//					appDetailsLinearLayout.addView(appIcon, appIconLayoutParams);
	//					appDetailsLinearLayout.addView(textView, appNameLayoutParams);
	//				}
	//			});
	//    	} 
	//	System.err.println("displayAppList: " + (System.currentTimeMillis() - currentTimeMillis));
	//	}

	//	private List<SearchableApplication> getAppList() {
	//	long currentTimeMillis = System.currentTimeMillis();
	//		final PackageManager pm = getPackageManager();
	//    	//get a list of installed apps.
	//    	List<ApplicationInfo> appInfos= pm.getInstalledApplications(PackageManager.GET_META_DATA);
	//    	List<SearchableApplication> apps = new LinkedList<SearchableApplication>();
	//    	for (ApplicationInfo applicationInfo : appInfos) {
	//			apps.add(new SearchableApplication(pm, applicationInfo));
	//		}
	//	long currentTimeMillis2 = System.currentTimeMillis();
	//	System.err.println("getAppList: " + (currentTimeMillis2 - currentTimeMillis));
	//		return apps;
	//	}

	private void addListeners_fork() {
		EditText searchBox = getSearchBox();
		searchBox.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				OnTextChangeEvent textChangeEvent = 
						new OnTextChangeEvent(s,start,before,count, System.nanoTime());
				startSearching_fork(textChangeEvent);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_app_man_home, menu);
		return true;
	}

	private void startSearching_fork(OnTextChangeEvent textChangeEvent) {
		System.out.println("User input "+ textChangeEvent.getCharSequence()+ 
				" time: " + System.nanoTime() + " nanos");
		CharSequence searchString = textChangeEvent.getCharSequence();
		for (final SearchThread<? extends BaseSearchProvider<? extends ISearchableElement>> searchThread 
				: searchThreads) {
			searchThread.setSearchString(searchString.toString(), textChangeEvent.getTimeOfEventInNano());
			State state = searchThread.getState();
			if(state == Thread.State.NEW){
				searchThread.start();
			} else{
				Thread thread = new Thread(){
					public void run() {
						Object objectLock = searchThread.getObjectLock();
						synchronized (objectLock) {
							objectLock.notifyAll();
						}
					};
				};
				thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
				thread.start();
			}
		}
	}

	private EditText getSearchBox(){
		if(searchBox == null)
			searchBox = (EditText)findViewById(R.id.searchBox);
		return searchBox;
	}

	/*
	 * Might be a long running function. Do not call on UI thread
	 */
	void addOrRemoveAppView(final ISearchableElement searchableElement, final Match match) {
		final View appView = searchableElement.getViewDetails().getSearchResultView();
		final boolean matches = match.isMatches();
		ViewDetails viewDetails = searchableElement.getViewDetails();
		final TextView primaryTextDisplay = viewDetails.getPrimaryTextDisplay();
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				if(matches){
					if(appView.getParent() == null){
							searchResultsView.addView(appView);
						searchResultsCount++;
					}
				} else {
					if(appView.getParent() == searchResultsView){
						searchResultsView.removeView(appView);
						searchResultsCount--;
					}
				}
				searchCountText.setText(String.valueOf(searchResultsCount));
				primaryTextDisplay.setText(match.getMatchedSpan(),BufferType.SPANNABLE);
			}
		});
	}
	
	

}
