package com.thewalkingschoolbus.thewalkingschoolbus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.api.ResultCallback;

import com.thewalkingschoolbus.thewalkingschoolbus.Interface.OnTaskComplete;
import com.thewalkingschoolbus.thewalkingschoolbus.Models.EnterGroupNameDialogFragment;
import com.thewalkingschoolbus.thewalkingschoolbus.Models.Group;
import com.thewalkingschoolbus.thewalkingschoolbus.Models.MapFragmentState;
import com.thewalkingschoolbus.thewalkingschoolbus.Models.User;
import com.thewalkingschoolbus.thewalkingschoolbus.api_binding.GetUserAsyncTask;
import com.thewalkingschoolbus.thewalkingschoolbus.map_modules.DirectionFinder;
import com.thewalkingschoolbus.thewalkingschoolbus.map_modules.DirectionFinderListener;
import com.thewalkingschoolbus.thewalkingschoolbus.map_modules.Route;
import com.thewalkingschoolbus.thewalkingschoolbus.Models.PlaceInfo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.thewalkingschoolbus.thewalkingschoolbus.api_binding.GetUserAsyncTask.functionType.ADD_MEMBER_TO_GROUP;
import static com.thewalkingschoolbus.thewalkingschoolbus.api_binding.GetUserAsyncTask.functionType.CREATE_GROUP;
import static com.thewalkingschoolbus.thewalkingschoolbus.api_binding.GetUserAsyncTask.functionType.LIST_GROUPS;

/*
* SOURCES - Based on following tutorials:
* Google Maps Android API tutorial
*   https://developers.google.com/maps/documentation/android-api/current-place-tutorial
* Youtube tutorial "Get Device Location" by "CodingWithMitch"
*   https://www.youtube.com/watch?v=fPFr0So1LmI
* Youtube tutorial "Basic Google Maps API Android Tutorial + Google Maps Directions API" by "Hiep Mai Thanh"
*   https://www.youtube.com/watch?v=fPFr0So1LmI
* map_modules from source code by "Hiep Mai Thanh"
*   https://github.com/hiepxuan2008/GoogleMapDirectionSimple
*/
public class MapFragment extends android.support.v4.app.Fragment implements GoogleMap.OnInfoWindowClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    // CUSTOM VALUES
    private static final float DEFAULT_ZOOM = 14; // Larger means more zoomed in
    private static final int MAXIMUM_ROUTE_DISTANCE_METERS = 10000000; // This distance is approx. Vancouver to Chilliwack
    private static int DEFAULT_SEARCH_RADIUS_METERS = 1000;

    private static final String TAG = "MapFragment";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1234;
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private GoogleMap mMap;
    private Boolean mLocationPermissionGranted = false;
    private Boolean mValidRouteEstablished = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private View view;
    private AutoCompleteTextView etOrigin;
    private AutoCompleteTextView etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private Circle searchRadiusCircle;
    private Location currentLocation;
    private static Route currentRoute;
    private static Group[] groupList;

    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));
    private PlaceInfo mPlace;
    private List<Marker> mMarker;

    // SET UP //

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (container != null) {
            container.removeAllViews();
        }
        view = inflater.inflate(R.layout.fragment_map, container, false);

        //if (isServicesOK()) // TODO: improve this
            getLocationPermission();


        etOrigin = (AutoCompleteTextView) view.findViewById(R.id.etOrigin);
        etDestination = (AutoCompleteTextView) view.findViewById(R.id.etDestination);
        Button btnFindPath = (Button) view.findViewById(R.id.btnFindPath);
        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findRoute();
            }
        });
        Button btnCreateGroup = (Button) view.findViewById(R.id.btnCreateGroup);
        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayCreateGroupDialog();
            }
        });
        Button btnViewGroup = (Button) view.findViewById(R.id.btnViewGroup);
        btnViewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getMapFragmentState() == MapFragmentState.JOIN_GROUP) {
                    displayGroupsNearbyCurrentPosition();
                } else if (getMapFragmentState() == MapFragmentState.MAP) {
                    displayNearbyGroups();
                } else {
                    Toast.makeText(getActivity(), "Unexpected error.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Disable unnecessary buttons according to Map State
        if (getMapFragmentState() == MapFragmentState.CREATE_GROUP) {
            btnViewGroup.setVisibility(View.GONE);
        } else if (getMapFragmentState() == MapFragmentState.JOIN_GROUP) {
            etOrigin.setVisibility(View.GONE);
            etDestination.setVisibility(View.GONE);
            btnFindPath.setVisibility(View.GONE);
            btnCreateGroup.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mGoogleApiClient!=null) {
            mGoogleApiClient.stopAutoManage(getActivity());
            mGoogleApiClient.disconnect();
        }
    }

    private void getLocationPermission() {
    /*
     * Request location permission, so that we can get the location of the device.
     * The result of the permission request is handled by a callback, onRequestPermissionsResult.
     */
        Log.d(TAG, "getLocationPermission: getting location permissions");

        if (ContextCompat.checkSelfPermission(this.getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            initMap();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");

        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
                initMap();
            }
        }
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");

        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "onMapReady: map is ready");

                mMap = googleMap;
                relocateMyLocationButton();

                if (mLocationPermissionGranted) {
                    init(); // TODO: review code for search auto complete
                    getDeviceLocation();
                    if (ActivityCompat.checkSelfPermission(getActivity(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getActivity(),
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
        });
    }

    // MAIN MAP FUNCTIONS - GENERAL //

    private void getDeviceLocation() {
    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            currentLocation = (Location) task.getResult(); // TODO: BUG: Occasionally, even though task.isSuccessful, current location will return null and crash app.
                            if (currentLocation == null) {
                                Toast.makeText(getActivity(), "Cannot find current location. (error code 1)", Toast.LENGTH_SHORT).show();
                            } else {
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            Toast.makeText(getActivity(), "Cannot find current location. (error code 2)", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void findRoute() {
        String origin = etOrigin.getText().toString();
        String destination = etDestination.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Draw route
            new DirectionFinder(new DirectionFinderListener() {
                @Override
                public void onDirectionFinderStart() {
                    displayProgressDialog();

                    if (originMarkers != null) {
                        for (Marker marker : originMarkers) {
                            marker.remove();
                        }
                    }

                    if (destinationMarkers != null) {
                        for (Marker marker : destinationMarkers) {
                            marker.remove();
                        }
                    }

                    if (polylinePaths != null) {
                        for (Polyline polyline:polylinePaths ) {
                            polyline.remove();
                        }
                    }

                    // TODO: keep better track of markers and removing them
                    if (mMarker != null) {
                        for (Marker marker : mMarker) {
                            marker.remove();
                        }
                    }
                    mMap.clear();
                }

                @Override
                public void onDirectionFinderSuccess(List<Route> routes) {
                    progressDialog.dismiss();

                    if (routes.size() < 0) {
                        Toast.makeText(getActivity(), "No result. Try rephrasing direction.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (isTooFar(routes.get(0).originLocation, routes.get(0).destinationLocation)) {
                            Toast.makeText(getActivity(), "Your route is too long!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mValidRouteEstablished = true;
                        polylinePaths = new ArrayList<>();
                        originMarkers = new ArrayList<>();
                        destinationMarkers = new ArrayList<>();

                        for (Route route : routes) {
                            moveCamera(route.destinationLocation, DEFAULT_ZOOM);
                            ((TextView) view.findViewById(R.id.tvDuration)).setText(route.duration.text);
                            ((TextView) view.findViewById(R.id.tvDistance)).setText(route.distance.text);

                            Log.d(TAG, "onDirectionFinderSuccess: Current route: FROM: " + route.originAddress + ", TO: " + route.destinationAddress);
                            currentRoute = route;

                            originMarkers.add(mMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.defaultMarker(210))
                                    .title("Origin")
                                    .snippet(route.originAddress)
                                    .position(route.originLocation)));
                            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.defaultMarker(210))
                                    .title("Destination")
                                    .snippet(route.destinationAddress)
                                    .position(route.destinationLocation)));

                            PolylineOptions polylineOptions = new PolylineOptions().
                                    geodesic(true).
                                    color(getResources().getColor(R.color.logoBlue)).
                                    width(10);

                            for (int i = 0; i < route.points.size(); i++)
                                polylineOptions.add(route.points.get(i));

                            polylinePaths.add(mMap.addPolyline(polylineOptions));
                        }
                    }
                }
            }, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void displayProgressDialog() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
    }

    // MAIN MAP FUNCTIONS - CREATE GROUP //

    private void displayCreateGroupDialog() {
        if (mValidRouteEstablished) {
            // Create dialog which calls createGroupDialogPositiveOnClick on OK.
            EnterGroupNameDialogFragment dialog = new EnterGroupNameDialogFragment();
            dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            dialog.show(getFragmentManager(), "MessageDialog");
        } else {
            Toast.makeText(getActivity(), "Please enter valid path first.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void createGroupDialogPositiveOnClick(final Context context, final AlertDialog alertDialog, String name) {
        if (Objects.equals(name, "")) {
            Toast.makeText(context, "Group name cannot be empty.", Toast.LENGTH_SHORT).show();
        } else {
            // Build group to add
            double[] routeLatArray = {currentRoute.originLocation.latitude, currentRoute.destinationLocation.latitude, 0};
            double[] routeLngArray = {currentRoute.originLocation.longitude, currentRoute.destinationLocation.longitude, 0};
            Group group = new Group(name, routeLatArray, routeLngArray);
            User user = new User();
            user.setId(User.getLoginUser().getId());
            group.setLeader(user);

            new GetUserAsyncTask(CREATE_GROUP, null, null, group,null, new OnTaskComplete() {
                @Override
                public void onSuccess(Object result) {
                    //Group[] group = (Group[]) result;
                    Toast.makeText(context, "Group created!", Toast.LENGTH_SHORT).show();
                    //joinGroup(context, (Group) result, false); // TODO: comment this out to disable auto join after creating group
                    //alertDialog.dismiss();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(context, "Name already exists.", Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).execute();
        }
    }

    // MAIN MAP FUNCTIONS - JOIN GROUP //

    private void displayNearbyGroups() {
        if (mValidRouteEstablished) {
            new GetUserAsyncTask(LIST_GROUPS, null, null, null,null, new OnTaskComplete() {
                @Override
                public void onSuccess(Object result) {
                    Boolean groupNearBy = false;
                    groupList = (Group[]) result;

                    // Display groups within search radius
                    for (Group group : groupList) {
                        // Compare destination difference of current route input and current group being examined. Returns distance in meters in results.
                        float[] results = new float[1];
                        Log.d(TAG, "displayNearbyGroups: distance between destinations in meters: " + results[0]);
                        Location.distanceBetween(currentRoute.destinationLocation.latitude, currentRoute.destinationLocation.longitude,
                                group.getRouteLatArray()[1], group.getRouteLngArray()[1],
                                results);
                        Log.d(TAG, "displayNearbyGroups: distance between destinations in meters: " + results[0]);
                        if (results[0] < DEFAULT_SEARCH_RADIUS_METERS) {
                            Log.d(TAG, "displayNearbyGroups: INSIDE CIRCLE: " + results[0]);
                            // Display this result on map.
                            displayGroup(group);
                            groupNearBy = true;
                        } else {
                            Log.d(TAG, "displayNearbyGroups: OUTSIDE CIRCLE: " + results[0]);
                        }
                    }
                    if (!groupNearBy) {
                        Toast.makeText(getActivity(), "No groups near destination.", Toast.LENGTH_SHORT).show();
                    }

                    // Draw visual circle for search radius
                    drawSearchRadius(currentRoute.destinationLocation, DEFAULT_SEARCH_RADIUS_METERS);
                    moveCamera(currentRoute.destinationLocation, DEFAULT_ZOOM);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getActivity(), "Failed to retrieve group data.", Toast.LENGTH_SHORT).show();
                    Toast.makeText(getActivity(), "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }).execute();
        } else {
            Toast.makeText(getActivity(), "Please enter valid path first.", Toast.LENGTH_SHORT).show();
        }
    }

    // Use this method to display all groups who's origin or destination is within search radius
    // Update currentLocation first, retrieve group from server, then check groups against currentLocation
    // TODO: Simplify method
    private void displayGroupsNearbyCurrentPosition() {
        try {
            if (mLocationPermissionGranted) {
                mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            currentLocation = (Location) task.getResult();
                            if (currentLocation == null) {
                                Toast.makeText(getActivity(), "Cannot find current location. (error code 1)", Toast.LENGTH_SHORT).show();
                            } else {
                                // Retrieve group data, and compare group to currentLocation
                                new GetUserAsyncTask(LIST_GROUPS, null, null, null,null, new OnTaskComplete() {
                                    @Override
                                    public void onSuccess(Object result) {
                                        if (result == null) {
                                            Toast.makeText(getActivity(), "Failed to retrieve data.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Boolean groupNearBy = false;
                                            groupList = (Group[]) result;

                                            // Display groups within search radius
                                            for (Group group : groupList) {
                                                Log.d(TAG, "currentGroup: ID:                 " + group.getId());
                                                Log.d(TAG, "currentGroup: DESCRIPTION:        " + group.getGroupDescription());
                                                Log.d(TAG, "currentGroup: ORIGIN LATLNG:      " + group.getRouteLatArray()[0] + ", " + group.getRouteLngArray()[0]);
                                                Log.d(TAG, "currentGroup: DESTINATION LATLNG: " + group.getRouteLatArray()[1] + ", " + group.getRouteLngArray()[1]);
                                                // Compare distance between current location to current group's DESTINATION
                                                float[] results = new float[1];
                                                Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                                        group.getRouteLatArray()[1], group.getRouteLngArray()[1],
                                                        results);
                                                if (results[0] < DEFAULT_SEARCH_RADIUS_METERS) {
                                                    // Display this result on map.
                                                    displayGroup(group);
                                                    groupNearBy = true;
                                                }
                                                // Compare distance between current location to current group's ORIGIN
                                                results = new float[1];
                                                Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                                                        group.getRouteLatArray()[0], group.getRouteLngArray()[0],
                                                        results);
                                                if (results[0] < DEFAULT_SEARCH_RADIUS_METERS) {
                                                    // Display this result on map.
                                                    displayGroup(group);
                                                    groupNearBy = true;
                                                }
                                            }
                                            if (!groupNearBy) {
                                                Toast.makeText(getActivity(), "No groups near you.", Toast.LENGTH_SHORT).show();
                                            }

                                            // Draw visual circle for search radius
                                            drawSearchRadius(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_SEARCH_RADIUS_METERS);
                                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception e) {

                                    }
                                }).execute();
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            Toast.makeText(getActivity(), "Cannot find current location. (error code 2)", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void displayGroup(final Group group) {
        // Enable info window click
        mMap.setOnInfoWindowClickListener(this);

        Log.d(TAG, "displayGroup: ORIGIN:      " + group.getRouteLatArray()[0] + ", " + group.getRouteLngArray()[0]);
        Log.d(TAG, "displayGroup: DESTINATION: " + group.getRouteLatArray()[1] + ", " + group.getRouteLngArray()[1]);

        try {
            new DirectionFinder(new DirectionFinderListener() {
                @Override
                public void onDirectionFinderStart() {

                }

                @Override
                public void onDirectionFinderSuccess(List<Route> routes) {
                    polylinePaths = new ArrayList<>();
                    originMarkers = new ArrayList<>();
                    destinationMarkers = new ArrayList<>();

                    for (Route route : routes) {
                        ((TextView) view.findViewById(R.id.tvDuration)).setText(route.duration.text);
                        ((TextView) view.findViewById(R.id.tvDistance)).setText(route.distance.text);

                        Marker marker = mMap.addMarker(new MarkerOptions()
                                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green)) // TODO: SET CUSTOM MARKER
                                        .title(group.getGroupDescription())
                                        .snippet("Origin: Click to join group!")
                                        .position(route.originLocation));
                        marker.setTag(group.getId());
                        originMarkers.add(marker);

                        marker = mMap.addMarker(new MarkerOptions()
                                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green)) // TODO: SET CUSTOM MARKER
                                        .title(group.getGroupDescription())
                                        .snippet("Destination: Click to join group!")
                                        .position(route.destinationLocation));
                        marker.setTag(group.getId());
                        destinationMarkers.add(marker);

                        PolylineOptions polylineOptions = new PolylineOptions().
                                geodesic(true).
                                color(getResources().getColor(R.color.colorAccent)).
                                width(8);

                        for (int i = 0; i < route.points.size(); i++)
                            polylineOptions.add(route.points.get(i));

                        polylinePaths.add(mMap.addPolyline(polylineOptions));
                    }
                }
            },
                    group.getRouteLatArray()[0] + ", " + group.getRouteLngArray()[0],
                    group.getRouteLatArray()[1] + ", " + group.getRouteLngArray()[1])
                    .execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (marker.getTag() == null) {
            return;
        }
        displayConfirmJoinGroup((String) marker.getTag());
    }

    private void displayConfirmJoinGroup(final String groupName) {

        // Group to join by name
        Group groupToJoin = new Group();
        for (Group group : groupList) {
            if (Objects.equals(group.getId(), groupName)) {
                groupToJoin = group;
            }
        }
        if (groupToJoin.getId() == null) {
            Toast.makeText(getActivity(), "Unexpected error.", Toast.LENGTH_SHORT).show();
            return;
        }
        final Group finalGroupToJoin = groupToJoin;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage("Join \"" + groupToJoin.getGroupDescription() + "\" ?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        joinGroup(getActivity(), finalGroupToJoin, true);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
    }

    private static void joinGroup(final Context context, final Group group, final boolean makeToasts) {
        if (group == null) {
            Toast.makeText(context, "Unexpected error.", Toast.LENGTH_SHORT).show();
        }
        User user = User.getLoginUser();
        new GetUserAsyncTask(ADD_MEMBER_TO_GROUP, user, null, group,null, new OnTaskComplete() {
            @Override
            public void onSuccess(Object result) {
                if (makeToasts) {
                    Toast.makeText(context, "Joined \"" + group.getGroupDescription() + "\" !", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                if(makeToasts) {
                    Toast.makeText(context, "Failed to join group.", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

    // HELPER FUNCTIONS //

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCameraSetMarker: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void drawSearchRadius(LatLng centerCoordinate, int radiusMeters) {
        if (searchRadiusCircle != null)
            searchRadiusCircle.remove();
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(centerCoordinate);
        circleOptions.radius(radiusMeters); //TODO: Calibrate to match search radius actual to visual
        circleOptions.strokeColor(0xFFffc300);
        circleOptions.fillColor(0x40f7f056);
        circleOptions.strokeWidth(4);
        searchRadiusCircle = mMap.addCircle(circleOptions);
    }

    private boolean isTooFar(LatLng origin, LatLng destination) {
        float[] results = new float[1];
        Location.distanceBetween(origin.latitude, origin.longitude,
                destination.latitude, destination.latitude,
                results);
        Log.d(TAG, "isTooFar: distance between origin and destination in meters: " + results[0]);
        return results[0] > MAXIMUM_ROUTE_DISTANCE_METERS;
    }

    private void relocateMyLocationButton() {
        // Get the button view
        View locationButton = ((View) view.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));

        // and next place it, for example, on bottom right (as Google Maps app)
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 30);
    }

    private MapFragmentState getMapFragmentState() {
        return MapFragmentState.values()[getArguments().getInt("state")];
    }

    // SEARCH AUTO COMPLETE CODE //

    private void init(){
        Log.d(TAG, "initializing");

        mGoogleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), this)
                .build();

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(getActivity(), mGoogleApiClient,
                LAT_LNG_BOUNDS, null);

        etOrigin.setOnItemClickListener(mAutocompleteClickListener);
        etDestination.setOnItemClickListener(mAutocompleteClickListener);

        etOrigin.setAdapter(mPlaceAutocompleteAdapter);
        etDestination.setAdapter(mPlaceAutocompleteAdapter);

        etOrigin.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == keyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){
                    // execute our method for searching
                    geoLocate();
                }
                return false;
            }
        });

        etDestination.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == keyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){
                    // execute our method for searching
                    geoLocate2();
                }
                return false;
            }
        });

        hideSoftKeyboard();
    }

    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = etOrigin.getText().toString();
        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException;" + e.getMessage());
        }

        if(list.size() >0 ){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate found a location " + address.toString());

            moveCameraSetMarker(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }

    private void geoLocate2() {
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = etDestination.getText().toString();
        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException;" + e.getMessage());
        }

        if(list.size() >0 ){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate found a location " + address.toString());

            moveCameraSetMarker(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }

    private void moveCameraSetMarker(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCameraSetMarker: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        mMap.clear();

        if(!title .equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMarker.add(mMap.addMarker(options));
        }
        hideSoftKeyboard();

        // Invalidate previously established route
        mValidRouteEstablished = false;
    }

    private void moveCameraSetMarker(LatLng latLng, float zoom, PlaceInfo placeInfo) {
        Log.d(TAG, "moveCameraSetMarker: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        mMap.clear();

        if(placeInfo != null){
            try {
                String snippet = placeInfo.getAddress();

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(placeInfo.getName())
                        .snippet(snippet);
                mMarker.add(mMap.addMarker(options));
            }catch (NullPointerException e) {
                Log.e(TAG, "moveCameraSetMarker: NullPointerException " + e.getMessage() );
            }
        }else {
            mMarker.add(mMap.addMarker(new MarkerOptions().position(latLng)));
        }

        hideSoftKeyboard();

        // Invalidate previously established route
        mValidRouteEstablished = false;
    }

    private void hideSoftKeyboard(){
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);

            try{
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                Log.d(TAG, "onResult: name: " + place.getName());
                mPlace.setAddress(place.getAddress().toString());
                Log.d(TAG, "onResult: address: " + place.getAddress());
//                mPlace.setAttributions(place.getAttributions().toString());
//                Log.d(TAG, "onResult: attributions: " + place.getAttributions());
                mPlace.setId(place.getId());
                Log.d(TAG, "onResult: id:" + place.getId());
                mPlace.setLatlng(place.getLatLng());
                Log.d(TAG, "onResult: latlng: " + place.getLatLng());
                mPlace.setRating(place.getRating());
                Log.d(TAG, "onResult: rating: " + place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(TAG, "onResult: phone number: " + place.getPhoneNumber());
                mPlace.setWebsiteUri(place.getWebsiteUri());
                Log.d(TAG, "onResult: website uri: " + place.getWebsiteUri());

                Log.d(TAG, "onResult: place: " + mPlace.toString());
            }catch (NullPointerException e){
                Log.e(TAG, "onResult: NullPointerException: " + e.getMessage() );
            }

            moveCameraSetMarker(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude), DEFAULT_ZOOM, mPlace);

            places.release();
        }
    };
}
