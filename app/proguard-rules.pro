# Add project specific ProGuard rules here.

# Preserve line numbers and source file names for crash analysis
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep data models used with Gson (Retrofit response deserialization)
-keep class com.gege.activityfindermobile.data.model.** { *; }
-keep class com.gege.activityfindermobile.data.dto.** { *; }
-keep class com.gege.activityfindermobile.data.api.ParticipantStatusUpdateRequest { *; }

# Gson generic type handling
-keepattributes Signature
-keepattributes *Annotation*

# Keep RecyclerView LayoutManagers used via XML reflection (app:layoutManager attribute)
-keep class androidx.recyclerview.widget.GridLayoutManager { *; }
-keep class androidx.recyclerview.widget.LinearLayoutManager { *; }
-keep class * extends androidx.recyclerview.widget.RecyclerView$LayoutManager { *; }

# Keep CircleImageView used in XML layouts (instantiated via reflection during inflation)
-keep class de.hdodenhof.circleimageview.CircleImageView { *; }

# Keep inner class DTO used with Gson serialization (not covered by data.dto.** rule)
-keep class com.gege.activityfindermobile.data.api.UserPhotoApiService$PhotoReorderRequest { *; }

# Keep Google Places SDK — uses ServiceLoader internally, R8 strips the provider constructors
-keep class com.google.android.libraries.places.** { *; }
-dontwarn com.google.android.libraries.places.**
