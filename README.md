[![Stand With Ukraine](https://raw.githubusercontent.com/vshymanskyy/StandWithUkraine/main/banner-direct-single.svg)](https://stand-with-ukraine.pp.ua)
[![Build status](https://build.appcenter.ms/v0.1/apps/6ef58ca2-721c-4622-bfe7-336d4c6d7d01/branches/master/badge)](https://appcenter.ms)
[![Upload to Firebase App Distribution](https://github.com/Turskyi/Travelling/actions/workflows/android_ci.yml/badge.svg?event=push)](https://github.com/Turskyi/Travelling/actions/workflows/android_ci.yml)
<img alt="GitHub commit activity" src="https://img.shields.io/github/commit-activity/m/Turskyi/travelling">

# Travelling

An Android mobile application, which gives a convenient way to collect all countries that you have
visited.

## PROJECT SPECIFICATION

• Programming language: [Kotlin](https://kotlinlang.org/);

• SDK: [Android](https://developer.android.com/studio/intro);

• Interface: [XML](https://developer.android.com/guide/topics/ui/declaring-layout);

• State management approach:
[ViewModel](https://developer.android.com/reference/androidx/lifecycle/ViewModel);

• HTTP client: [Retrofit](https://square.github.io/retrofit/);

• Database: [Room](https://developer.android.com/training/data-storage/room);

• Dependency injection: [Koin](https://insert-koin.io/docs/reference/introduction);

• Reactive programming: [Coroutines](https://developer.android.com/kotlin/coroutines);

• Version control system: [Git](https://git-scm.com);

• Git Hosting Service: [GitHub](https://github.com);

• CI/CD: [GitHub Actions](https://docs.github.com/en/actions) is used to deliver new Android
Package (APK) to [Firebase App Distribution](https://firebase.google.com/docs/app-distribution)
after every push to the **dev** branch,
[Visual Studio App Center](https://docs.microsoft.com/en-us/appcenter/) is used to
[deliver](https://appcenter.ms/users/Turskyi/apps/Travelling/build/branches/master) new release
app bundle to **Google Play** after every push to **master** branch;

• App testing platforms:
[Firebase App Distribution](https://appdistribution.firebase.dev/i/c6a7f44dbe6de66d);

• App store: [Google Play](https://play.google.com/store/apps/details?id=ua.turskyi.travelling);

• Operating system: [Android](https://www.android.com/);

• Embedded SDK: [Facebook Sharing](https://developers.facebook.com/docs/sharing/android);

• Google Play services: [Location](https://developer.android.com/training/location);

• Cloud services: [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging);

• UI components:
[Lottie](https://lottiefiles.com/what-is-lottie),
[PhotoView](https://github.com/Baseflow/PhotoView),
[ViewPager2](https://developer.android.com/jetpack/androidx/releases/viewpager2),
[Data chart](https://weeklycoding.com/mpandroidchart/),
[RecyclerView](http://www.recyclerview.org/),
[Loading SVG](https://github.com/corouteam/GlideToVectorYou),
[Glide](https://bumptech.github.io/glide/),
[Data Binding](https://developer.android.com/topic/libraries/data-binding);

• Api: https://restcountries.com/#api-endpoints-v2-all;

• Architecture Components:
[Paging](https://developer.android.com/topic/libraries/architecture/paging),
[LiveData](https://developer.android.com/topic/libraries/architecture/livedata),
[ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel);

• Architectural pattern:
<br>
<a href="https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel">
<img src="documentation/android_model_view_viewmodel.jpeg" width="800" >
</a>
</br>

• Screenshots:
<!--suppress CheckImageSize -->
<img src="screenshots/device-2020-06-05-085243.png" width="200"  alt="Screenshot of home page">
<img src="screenshots/device-2020-06-05-085456.png" width="200"  alt="Screenshot of a page with list of countries">
<img src="screenshots/device-2020-06-05-090524.png" width="200"  alt="Screenshot of a page with a flag">
<img src="screenshots/device-2020-06-28-164528.png" width="300"  alt="Screenshot of a home page with a bottom sheet">
<img src="screenshots/device-2020-10-18-103522.png" width="200"  alt="Screenshot of a home page with the loading indicator">
<img src="screenshots/device-2020-10-18-103111.png" width="200"  alt="Screenshot of a dialog">
<img src="screenshots/device-2020-06-05-090129.png" width="200"  alt="Screenshot">
<img src="screenshots/device-2020-06-05-091508.png" width="200"  alt="Screenshot of a page with my photo">
<img src="screenshots/device-2020-06-05-094730.png" width="300"  alt="Screenshot of a tablet with the list of countries">
<img src="screenshots/device-2020-06-28-162902.png" width="400"  alt="Screenshot of an info dialog">

• **Code Readability:** code is easily readable with no unnecessary blank lines, no unused variables
or methods, and no commented-out code, all variables, methods, and resource IDs are descriptively
named such that another developer reading the code can easily understand their function.

## Download

<a href="https://play.google.com/store/apps/details?id=ua.turskyi.travelling" target="_blank">
<img src="https://play.google.com/intl/en_gb/badges/static/images/badges/en_badge_web_generic.png" width=240  alt="google play badge"/>
</a>
