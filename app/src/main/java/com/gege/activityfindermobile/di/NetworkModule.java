package com.gege.activityfindermobile.di;

import com.gege.activityfindermobile.data.api.ActivityApiService;
import com.gege.activityfindermobile.data.api.ParticipantApiService;
import com.gege.activityfindermobile.data.api.ReviewApiService;
import com.gege.activityfindermobile.data.api.UserApiService;
import com.gege.activityfindermobile.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    @Singleton
    public Gson provideGson() {
        return new GsonBuilder()
                .setLenient()
                .create();
    }

    @Provides
    @Singleton
    public HttpLoggingInterceptor provideLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(HttpLoggingInterceptor loggingInterceptor) {
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient, Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Provides
    @Singleton
    public UserApiService provideUserApiService(Retrofit retrofit) {
        return retrofit.create(UserApiService.class);
    }

    @Provides
    @Singleton
    public ActivityApiService provideActivityApiService(Retrofit retrofit) {
        return retrofit.create(ActivityApiService.class);
    }

    @Provides
    @Singleton
    public ParticipantApiService provideParticipantApiService(Retrofit retrofit) {
        return retrofit.create(ParticipantApiService.class);
    }

    @Provides
    @Singleton
    public ReviewApiService provideReviewApiService(Retrofit retrofit) {
        return retrofit.create(ReviewApiService.class);
    }
}
