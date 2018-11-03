package com.diyandroid.eazycampus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.diyandroid.eazycampus.activity.LoginPage;

public abstract class ExceptionHandlingAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, AsyncTaskResult<Result>> {
    @SuppressLint("StaticFieldLeak")
    private Context context;

    public ExceptionHandlingAsyncTask(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    protected AsyncTaskResult<Result> doInBackground(Params... params) {
        try {
            return new AsyncTaskResult<>(doInBackground2(params));
        } catch (Exception e) {
            return new AsyncTaskResult<>(e);
        }
    }

    @Override
    protected void onPostExecute(AsyncTaskResult<Result> result) {
        if (result.getError() != null) {
            onPostException(result.getError());
        } else {
            onPostExecute2(result.getResult());
        }
        super.onPostExecute(result);
    }

    protected abstract Result doInBackground2(Params... params);

    protected abstract void onPostExecute2(Result result);

    protected void onPostException(Exception exception) {
        Toast.makeText(context, "Network Problem! Please try after few minutes.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(context, LoginPage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        context.startActivity(intent);
    }
}