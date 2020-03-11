package com.diyandroid.eazycampus.activity;


import androidx.appcompat.app.AppCompatActivity;

public class MarksPage extends AppCompatActivity  {

//    RadioButton internalMaks, sessionalMarks;
//    Map<String, String> loginCookies;
//    String jsonCookies;
//    Element markTable;
//
//    ListView mListView;
//    ArrayList<Marks> peopleList;
//
//    FirebaseRemoteConfig mFirebaseRemoteConfig;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_marks_page);
//
//        Toast.makeText(this, "Marks feature aren't added to this version yet!", Toast.LENGTH_LONG).show();
//
//        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
//                .setDeveloperModeEnabled(BuildConfig.DEBUG)
//                .build();
//
//        mFirebaseRemoteConfig.setConfigSettings(configSettings);
//        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
//
////        mFirebaseRemoteConfig.fetch(0)
////                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
////                    @Override
////                    public void onComplete(@NonNull Task<Void> task) {
////                        if (task.isSuccessful()) {
////                            mFirebaseRemoteConfig.activateFetched();
////                        }
////                    }
////                });
//
//        Button submit = findViewById(R.id.submitMarks);
//
//        Toolbar toolbar = findViewById(R.id.toolbarMarks);
//        setSupportActionBar(toolbar);
//
//        // toolbar fancy stuff
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle("Evaluation Marks");
//
//        jsonCookies = getIntent().getStringExtra("COOKIES");
//
//        loginCookies = new Gson().fromJson(jsonCookies, new TypeToken<Map<String, String>>() {
//        }.getType());
//
//        internalMaks = findViewById(R.id.internal);
//        sessionalMarks = findViewById(R.id.sessional);
//
//        internalMaks.setOnClickListener(this);
//        sessionalMarks.setOnClickListener(this);
//
////        new getInternalMarks(this).execute();
//
//        submit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (internalMaks.isChecked()) {
//                    sessionalMarks.setChecked(false);
//                    new getInternalMarks(getApplicationContext()).execute();
//                }
//
//                if (sessionalMarks.isChecked()) {
//                    internalMaks.setChecked(false);
//                    new getSessionalMarks(getApplicationContext()).execute();
//                }
//            }
//        });
//
//    }
//
//    private boolean parsingSuccessful;
//
//    @SuppressLint("StaticFieldLeak")
//    private class getSessionalMarks extends ExceptionHandlingAsyncTask<String, Void, Element> {
//
//        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressMarks);
//        Elements row;
//        int column;
//
//        Connection.Response response;
//
//        public getSessionalMarks(Context context) {
//            super(context);
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            parsingSuccessful = true;
//            //Progress bar implementations
//            progressBar.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected Element doInBackground2(String... strings) {
//            try {
//                Document evaluationPage = Jsoup.connect(getString(R.string.tkmce_marks_page))
//                        .cookies(loginCookies)
//                        .referrer("http://210.212.227.210/tkmce/index.aspx")
//                        .followRedirects(true)
//                        .userAgent("Mozilla")
//                        .data("Upgrade-Insecure-Requests", "1")
//                        .method(Connection.Method.GET)
//                        .timeout(30 * 1000)
//                        .execute().parse();
//
//                response = Jsoup.connect(getString(R.string.tkmce_marks_page))
//                        .cookies(loginCookies)
//                        .referrer(getString(R.string.tkmce_marks_page))
//                        .userAgent("Mozilla")
//                        .data("__EVENTTARGET", "")
//                        .data("__EVENTARGUMENT", "")
//                        .data("__LASTFOCUS", "")
//                        .data("__VIEWSTATE", evaluationPage.getElementById("__VIEWSTATE").val())
//                        .data("__VIEWSTATEGENERATOR", evaluationPage.getElementById("__VIEWSTATEGENERATOR").val())
//                        .data("__EVENTVALIDATION", evaluationPage.getElementById("__EVENTVALIDATION").val())
//                        .data("ctl00$hdnisclose", evaluationPage.getElementById("ctl00_hdnisclose").val())
//                        .data("ctl00$ContentPlaceHolder1$rbtnReportType", "1")
//                        .data("ctl00$ContentPlaceHolder1$btnView", "View")
//                        .data("ctl00$HiddenField1", evaluationPage.getElementById("ctl00_HiddenField1").val())
//                        .followRedirects(true)
//                        .data("Upgrade-Insecure-Requests", "1")
//                        .method(Connection.Method.POST)
//                        .timeout(30 * 1000)
//                        .execute();
//
//                evaluationPage = response.parse();
//                markTable = evaluationPage.getElementById(mFirebaseRemoteConfig.getString("MARKS_CONTENT_PLACEHOLDER"));
//
//            } catch (IOException ex) {
//                ex.printStackTrace();
//                parsingSuccessful = false;
//            }
//
//            return markTable;
//        }
//
//        @Override
//        protected void onPostExecute2(Element element) {
//            progressBar.setVisibility(View.GONE);
//
//            if (parsingSuccessful && markTable != null && response.statusCode() == 200) {
//                row = markTable.getElementsByTag("table").get(1).getElementsByTag("tr").get(0).getElementsByTag("td");
//
//                markTable = markTable.getElementsByTag("table").get(1);
//
//                mListView = findViewById(R.id.listMarks);
//
//                CardView cardView = findViewById(R.id.cardView5);
//                cardView.setVisibility(View.VISIBLE);
//
//                peopleList = new ArrayList<>();
//
//
//                row = markTable.getElementsByTag("tr"); //count no of rows
//                column = markTable.getElementsByTag("tr").get(0).getElementsByTag("td").size(); //count no of columns
//
//                Elements details;
//                ArrayList<String> marksList;
//
//                for (int i = 0; i < row.size(); i++) {
//                    marksList = new ArrayList<>();
//
//                    details = row.get(i).getElementsByTag("td");
//                    for (int j = 0; j < column; j++) {
//                        marksList.add(details.get(j).text());
//                    }
//                    peopleList.add(new Marks(marksList));
//                }
//
//                MarksListAdapter adapter = new MarksListAdapter(MarksPage.this, R.layout.adapter_marks, peopleList);
//                mListView.setAdapter(adapter);
//            } else {
//                Toast.makeText(MarksPage.this, "Failed retrieving data!", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    //Menu bar item click
//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.internal:
//                sessionalMarks.setChecked(false);
//                break;
//            case R.id.sessional:
//                internalMaks.setChecked(false);
//                break;
//        }
//    }
//
//    //Closing Activity with back button
//    @Override
//    public boolean onSupportNavigateUp() {
//        finish();
//        return true;
//    }
//
//    private class getInternalMarks extends ExceptionHandlingAsyncTask<String, Void, Element> {
//        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressMarks);
//        Elements row;
//        int column;
//
//        Connection.Response response;
//
//        public getInternalMarks(Context context) {
//            super(context);
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            parsingSuccessful = true;
//            progressBar.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected Element doInBackground2(String... strings) {
//            try {
//                Document evaluationPage = Jsoup.connect(getString(R.string.tkmce_marks_page))
//                        .cookies(loginCookies)
//                        .referrer(getString(R.string.tkmce_index_url))
//                        .followRedirects(true)
//                        .userAgent("Mozilla")
//                        .data("Upgrade-Insecure-Requests", "1")
//                        .method(Connection.Method.GET)
//                        .timeout(30 * 1000)
//                        .execute().parse();
//
//                response = Jsoup.connect(getString(R.string.tkmce_marks_page))
//                        .cookies(loginCookies)
//                        .referrer(getString(R.string.tkmce_marks_page))
//                        .data("__EVENTTARGET", "")
//                        .data("__EVENTARGUMENT", "")
//                        .userAgent("Mozilla")
//                        .data("__LASTFOCUS", "")
//                        .data("__VIEWSTATE", evaluationPage.getElementById("__VIEWSTATE").val())
//                        .data("__VIEWSTATEGENERATOR", evaluationPage.getElementById("__VIEWSTATEGENERATOR").val())
//                        .data("__EVENTVALIDATION", evaluationPage.getElementById("__EVENTVALIDATION").val())
//                        .data("ctl00$hdnisclose", evaluationPage.getElementById("ctl00_hdnisclose").val())
//                        .data(mFirebaseRemoteConfig.getString("MARKS_REPORT_TYPE"), mFirebaseRemoteConfig.getString("MARKS_REPORT_TYPE_VALUE"))
//                        .data(mFirebaseRemoteConfig.getString("MARKS_PLACEHOLDER_BTN_VIEW"), mFirebaseRemoteConfig.getString("MARKS_PLACEHOLDER_BTN_VIEW_VALUE"))
////                        .data(mFirebaseRemoteConfig.getString("MARKS_EXTRA_FIELD1"), mFirebaseRemoteConfig.getString("MARKS_EXTRA_FIELD2"))
//                        .data("ctl00$HiddenField1", evaluationPage.getElementById("ctl00_HiddenField1").val())
//                        .followRedirects(true)
//                        .data("Upgrade-Insecure-Requests", "1")
//                        .method(Connection.Method.POST)
//                        .timeout(30 * 1000)
//                        .execute();
//
//                evaluationPage = response.parse();
//                markTable = evaluationPage.getElementById(mFirebaseRemoteConfig.getString("MARKS_CONTENT_PLACEHOLDER"));
//
//            } catch (IOException ex) {
//                ex.printStackTrace();
//                parsingSuccessful = false;
//            }
//
//            return markTable;
//        }
//
//        @Override
//        protected void onPostExecute2(Element result) {
//            progressBar.setVisibility(View.GONE);
//
//            if (parsingSuccessful && markTable != null && response.statusCode() == 200) {
//                markTable = markTable.getElementsByTag("table").get(1);
//
//                mListView = findViewById(R.id.listMarks);
//
//                CardView cardView = findViewById(R.id.cardView5);
//                cardView.setVisibility(View.VISIBLE);
//
//                peopleList = new ArrayList<>();
//
//                row = markTable.getElementsByTag("tr"); //count no of rows
//                column = markTable.getElementsByTag("tr").get(0).getElementsByTag("td").size(); //count no of columns
//
//                Elements details;
//                ArrayList<String> marksList;
//
//                for (int i = 0; i < row.size(); i++) {
//                    marksList = new ArrayList<>();
//
//                    details = row.get(i).getElementsByTag("td");
//                    for (int j = 0; j < column; j++) {
//                        marksList.add(details.get(j).text());
//                    }
//                    peopleList.add(new Marks(marksList));
//                }
//                MarksListAdapter adapter = new MarksListAdapter(MarksPage.this, R.layout.adapter_marks, peopleList);
//                mListView.setAdapter(adapter);
//            } else {
//                Toast.makeText(MarksPage.this, "Failed retrieving data!", Toast.LENGTH_SHORT).show();
//            }
//        }
//
//
//        @Override
//        protected void onPostException(Exception exception) {
//            super.onPostException(exception);
//        }
//    }
}