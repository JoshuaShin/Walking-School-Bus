package com.thewalkingschoolbus.thewalkingschoolbus.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.thewalkingschoolbus.thewalkingschoolbus.R;
import com.thewalkingschoolbus.thewalkingschoolbus.api_binding.GetUserAsyncTask;
import com.thewalkingschoolbus.thewalkingschoolbus.interfaces.OnTaskComplete;
import com.thewalkingschoolbus.thewalkingschoolbus.models.Customization;
import com.thewalkingschoolbus.thewalkingschoolbus.models.User;
import com.thewalkingschoolbus.thewalkingschoolbus.models.collections.Avatar;
import com.thewalkingschoolbus.thewalkingschoolbus.models.collections.Theme;
import com.thewalkingschoolbus.thewalkingschoolbus.models.collections.Title;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.thewalkingschoolbus.thewalkingschoolbus.api_binding.GetUserAsyncTask.functionType.EDIT_USER;
import static com.thewalkingschoolbus.thewalkingschoolbus.api_binding.GetUserAsyncTask.functionType.GET_USER_BY_ID;

public class CollectionFragment extends android.app.Fragment {

    private static final String TAG = "CollectionFragment";
    private View view;
    private Avatar[] avatars;
    private Title[] titles;
    private Theme[] themes;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (container != null) {
            container.removeAllViews();
        }
        view = inflater.inflate(R.layout.fragment_collection, container, false);

        // TEST
        //resetUnlocks();
        setupAddTestPoints();

        setupCustomizationInfo();
        updateListViewTitles();
        updateListViewThemes();
        updateAvatarSelection();
        updatePointsView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void setupAddTestPoints() {
        Button button = view.findViewById(R.id.addTestPoints);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User.getLoginUser().addPoints(100);
                updatePointsView();

                // Save to server
                new GetUserAsyncTask(EDIT_USER, User.getLoginUser(), null, null,null, new OnTaskComplete() {
                    @Override
                    public void onSuccess(Object result) {
                        Toast.makeText(getActivity(), "+ 100", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAG, "Error: "+e.getMessage());
                    }
                }).execute();
            }
        });
    }

    private void setupCustomizationInfo() {
        avatars = Avatar.avatars;
        titles = Title.titles;
        themes = Theme.themes;

        // Reset unlock
        for (Avatar avatar : avatars) {
            avatar.setAvailable(false);
        }
        for (Title title : titles) {
            title.setAvailable(false);
        }
        for (Theme theme : themes) {
            theme.setAvailable(false);
        }

        // Update unlock
        if (User.getLoginUser().getCustomization() != null) {
            int[] avatarOwned = User.getLoginUser().getCustomization().getAvatarOwned();
            if (avatarOwned != null) {
                for (int i = 0; i < avatarOwned.length; i ++) {
                    avatars[avatarOwned[i]].setAvailable(true);
                }
            }
            int[] titleOwned = User.getLoginUser().getCustomization().getTitleOwned();
            if (titleOwned != null) {
                for (int i = 0; i < titleOwned.length; i ++) {
                    titles[titleOwned[i]].setAvailable(true);
                }
            }
            int[] themeOwned = User.getLoginUser().getCustomization().getThemeOwned();
            if (themeOwned != null) {
                for (int i = 0; i < themeOwned.length; i ++) {
                    themes[themeOwned[i]].setAvailable(true);
                }
            }
        }
    }

    private void updatePointsView() {
        TextView textView = view.findViewById(R.id.userPoints);
        textView.setText(User.getLoginUser().getCurrentPoints() + " Point(s)"); // TODO: does not call to server
    }

    private ImageButton[] imageButtonAvatars;

    private void updateAvatarSelection() {
        imageButtonAvatars = new ImageButton[avatars.length];
        for (int i = 0; i < imageButtonAvatars.length; i++) {
            imageButtonAvatars[i] = new ImageButton(getActivity());
            imageButtonAvatars[i].setImageResource(getImageId(getActivity(), avatars[i].getName()));
            imageButtonAvatars[i].setAdjustViewBounds(true);
            imageButtonAvatars[i].setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageButtonAvatars[i].setBackgroundColor(Color.TRANSPARENT);
            imageButtonAvatars[i].setTag(i);
            imageButtonAvatars[i].setLayoutParams(new LinearLayout.LayoutParams(240, ViewGroup.LayoutParams.WRAP_CONTENT));
            imageButtonAvatars[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickAvatar((ImageButton) view);
                }
            });
            LinearLayout avatarHolder = view.findViewById(R.id.AvatarHolder);
            avatarHolder.addView(imageButtonAvatars[i]);

            // Darken locked avatar
            if (!avatars[i].isAvailable()) {
                imageButtonAvatars[i].setColorFilter(Color.BLACK);
            }
            // Highlight equipped avatar
            if (User.getLoginUser().getCustomization() != null && User.getLoginUser().getCustomization().getAvatarEquipped() == i) {
                imageButtonAvatars[i].setBackgroundColor(Color.LTGRAY);
            }
        }
    }

    private void onClickAvatar(final ImageButton imageButton) {
        final Avatar avatarClicked = avatars[(int) imageButton.getTag()];
        if (avatarClicked.isAvailable()) {

            // Update avatar equipped
            if (User.getLoginUser().getCustomization() == null) {
                User.getLoginUser().setCustomization(new Customization());
            }
            User.getLoginUser().getCustomization().setAvatarEquipped((int) imageButton.getTag());

            new GetUserAsyncTask(EDIT_USER, User.getLoginUser(), null, null,null, new OnTaskComplete() {
                @Override
                public void onSuccess(Object result) {
                    Toast.makeText(getActivity(), "Selected!", Toast.LENGTH_SHORT).show();
                    for (ImageButton button : imageButtonAvatars) {
                        button.setBackgroundColor(Color.TRANSPARENT);
                    }
                    imageButton.setBackgroundColor(Color.LTGRAY);
                }
                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Error: "+e.getMessage());
                }
            }).execute();

        } else {
            if (User.getLoginUser().addPoints(- avatarClicked.getCost())) { // Update points
                updatePointsView();

                // Update avatar purchased
                if (User.getLoginUser().getCustomization() == null) {
                    User.getLoginUser().setCustomization(new Customization());
                }
                if (User.getLoginUser().getCustomization().getAvatarOwned() == null) {
                    int[] avatarOwnedUpdate = new int[]{(int) imageButton.getTag()};
                    User.getLoginUser().getCustomization().setAvatarOwned(avatarOwnedUpdate);
                } else {
                    Log.d(TAG, "$$$$ " + Arrays.toString(User.getLoginUser().getCustomization().getAvatarOwned()));
                    int[] avatarOwned = User.getLoginUser().getCustomization().getAvatarOwned();
                    int[] avatarOwnedUpdate = new int[avatarOwned.length + 1];
                    System.arraycopy(avatarOwned, 0, avatarOwnedUpdate, 0, avatarOwned.length );
                    avatarOwnedUpdate[avatarOwnedUpdate.length - 1] = (int) imageButton.getTag();
                    User.getLoginUser().getCustomization().setAvatarOwned(avatarOwnedUpdate);
                    Log.d(TAG, "$$$$ " + Arrays.toString(User.getLoginUser().getCustomization().getAvatarOwned()));
                }

                // Save to server
                new GetUserAsyncTask(EDIT_USER, User.getLoginUser(), null, null,null, new OnTaskComplete() {
                    @Override
                    public void onSuccess(Object result) {
                        Toast.makeText(getActivity(), "Purchased", Toast.LENGTH_SHORT).show();
                        imageButton.setColorFilter(Color.TRANSPARENT);
                        avatarClicked.setAvailable(true);
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAG, "Error: "+e.getMessage());
                    }
                }).execute();
            } else {
                Toast.makeText(getActivity(), "Too expensive", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateListViewTitles() {
        // Build list
        List<String> monitoringList = new ArrayList<>();
        for(Title title: titles){
            if (title.isAvailable()) {
                monitoringList.add(title.getTitle());
            } else {
                monitoringList.add(title.getTitle() + " (LOCKED " + title.getCost() + "PTS)" );
            }
        }

        // Build adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.collection_entry, monitoringList);

        // Configure the list view
        ListView list = view.findViewById(R.id.listViewTitles);
        list.setAdapter(adapter);

        // Update clicks
        registerClickCallbackTitles();

        // Highlight equipped title
        highlightEquippedTitle();
    }

    private void highlightEquippedTitle() {
        if (User.getLoginUser().getCustomization() != null && User.getLoginUser().getCustomization().getTitleEquipped() != -1) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ListView listView = view.findViewById(R.id.listViewTitles);
                    listView.getChildAt(User.getLoginUser().getCustomization().getTitleEquipped()).setBackgroundColor(Color.LTGRAY);
                }
            }, 1);
        }
    }

    private void registerClickCallbackTitles() {
        final ListView listView = view.findViewById(R.id.listViewTitles);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, final int position, long id) {
                if (titles[position].isAvailable()) {

                    // Update title equipped
                    if (User.getLoginUser().getCustomization() == null) {
                        User.getLoginUser().setCustomization(new Customization());
                    }
                    User.getLoginUser().getCustomization().setTitleEquipped(position);

                    new GetUserAsyncTask(EDIT_USER, User.getLoginUser(), null, null,null, new OnTaskComplete() {
                        @Override
                        public void onSuccess(Object result) {
                            Toast.makeText(getActivity(), "Selected!", Toast.LENGTH_SHORT).show();
                            for (int i = 0; i < listView.getChildCount(); i++) {
                                listView.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                            }
                            listView.getChildAt(position).setBackgroundColor(Color.LTGRAY);
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Log.d(TAG, "Error: "+e.getMessage());
                        }
                    }).execute();

                } else {
                    if (User.getLoginUser().addPoints(- titles[position].getCost())) {
                        updatePointsView();

                        // Update title purchased
                        if (User.getLoginUser().getCustomization() == null) {
                            User.getLoginUser().setCustomization(new Customization());
                        }
                        if (User.getLoginUser().getCustomization().getTitleOwned() == null) {
                            int[] titleOwnedUpdate = new int[]{position};
                            User.getLoginUser().getCustomization().setTitleOwned(titleOwnedUpdate);
                        } else {
                            Log.d(TAG, "$$$$ " + Arrays.toString(User.getLoginUser().getCustomization().getTitleOwned()));
                            int[] titleOwned = User.getLoginUser().getCustomization().getTitleOwned();
                            int[] titleOwnedUpdate = new int[titleOwned.length + 1];
                            System.arraycopy(titleOwned, 0, titleOwnedUpdate, 0, titleOwned.length );
                            titleOwnedUpdate[titleOwnedUpdate.length - 1] = position;
                            User.getLoginUser().getCustomization().setTitleOwned(titleOwnedUpdate);
                            Log.d(TAG, "$$$$ " + Arrays.toString(User.getLoginUser().getCustomization().getTitleOwned()));
                        }

                        new GetUserAsyncTask(EDIT_USER, User.getLoginUser(), null, null,null, new OnTaskComplete() {
                            @Override
                            public void onSuccess(Object result) {
                                Toast.makeText(getActivity(), "Purchased", Toast.LENGTH_SHORT).show();
                                TextView textView = (TextView) listView.getChildAt(position);
                                textView.setText(titles[position].getTitle());
                                titles[position].setAvailable(true);
                            }
                            @Override
                            public void onFailure(Exception e) {
                                Log.d(TAG, "Error: "+e.getMessage());
                            }
                        }).execute();
                    } else {
                        Toast.makeText(getActivity(), "Too expensive", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void updateListViewThemes() {
        // Build list
        List<String> monitoringList = new ArrayList<>();
        for(Theme theme: themes){
            if (theme.isAvailable()) {
                monitoringList.add(theme.getName());
            } else {
                monitoringList.add(theme.getName() + " (LOCKED " + theme.getCost() + "PTS)" );
            }
        }

        // Build adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.collection_entry, monitoringList);

        // Configure the list view
        ListView list = view.findViewById(R.id.listViewThemes);
        list.setAdapter(adapter);

        // Update clicks
        registerClickCallbackThemes();

        // Highlight equipped title
        highlightEquippedTheme();
    }

    private void highlightEquippedTheme() {
        if (User.getLoginUser().getCustomization() != null && User.getLoginUser().getCustomization().getThemeEquipped() != -1) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ListView listView = view.findViewById(R.id.listViewThemes);
                    listView.getChildAt(User.getLoginUser().getCustomization().getThemeEquipped()).setBackgroundColor(Color.LTGRAY);
                }
            }, 1);
        }
    }

    private void registerClickCallbackThemes() {
        final ListView listView = view.findViewById(R.id.listViewThemes);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, final int position, long id) {
                if (themes[position].isAvailable()) {

                    // Update theme equipped
                    if (User.getLoginUser().getCustomization() == null) {
                        User.getLoginUser().setCustomization(new Customization());
                    }
                    User.getLoginUser().getCustomization().setThemeEquipped(position);

                    new GetUserAsyncTask(EDIT_USER, User.getLoginUser(), null, null,null, new OnTaskComplete() {
                        @Override
                        public void onSuccess(Object result) {
                            Toast.makeText(getActivity(), "Selected!", Toast.LENGTH_SHORT).show();
                            for (int i = 0; i < listView.getChildCount(); i++) {
                                listView.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                            }
                            listView.getChildAt(position).setBackgroundColor(Color.LTGRAY);
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Log.d(TAG, "Error: "+e.getMessage());
                        }
                    }).execute();

                } else {
                    if (User.getLoginUser().addPoints(- themes[position].getCost())) {
                        updatePointsView();

                        // Update theme purchased
                        if (User.getLoginUser().getCustomization() == null) {
                            User.getLoginUser().setCustomization(new Customization());
                        }
                        if (User.getLoginUser().getCustomization().getThemeOwned() == null) {
                            int[] themeOwnedUpdate = new int[]{position};
                            User.getLoginUser().getCustomization().setThemeOwned(themeOwnedUpdate);
                        } else {
                            Log.d(TAG, "$$$$ " + Arrays.toString(User.getLoginUser().getCustomization().getThemeOwned()));
                            int[] themeOwned = User.getLoginUser().getCustomization().getThemeOwned();
                            int[] themeOwnedUpdate = new int[themeOwned.length + 1];
                            System.arraycopy(themeOwned, 0, themeOwnedUpdate, 0, themeOwned.length );
                            themeOwnedUpdate[themeOwnedUpdate.length - 1] = position;
                            User.getLoginUser().getCustomization().setThemeOwned(themeOwnedUpdate);
                            Log.d(TAG, "$$$$ " + Arrays.toString(User.getLoginUser().getCustomization().getThemeOwned()));
                        }

                        new GetUserAsyncTask(EDIT_USER, User.getLoginUser(), null, null,null, new OnTaskComplete() {
                            @Override
                            public void onSuccess(Object result) {
                                Toast.makeText(getActivity(), "Purchased", Toast.LENGTH_SHORT).show();
                                TextView textView = (TextView) listView.getChildAt(position);
                                textView.setText(themes[position].getName());
                                themes[position].setAvailable(true);
                            }
                            @Override
                            public void onFailure(Exception e) {
                                Log.d(TAG, "Error: "+e.getMessage());
                            }
                        }).execute();
                    } else {
                        Toast.makeText(getActivity(), "Too expensive", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public static int getImageId(Context context, String imageName) {
        return context.getResources().getIdentifier("drawable/" + imageName, null, context.getPackageName());
    }

    // TEST

    private void resetUnlocks() {
        int[] avatarOwnedUpdate = new int[] {};
        User.getLoginUser().getCustomization().setAvatarOwned(avatarOwnedUpdate);
        User.getLoginUser().getCustomization().setAvatarEquipped(-1);
        int[] titleOwnedUpdate = new int[] {};
        User.getLoginUser().getCustomization().setTitleOwned(titleOwnedUpdate);
        User.getLoginUser().getCustomization().setTitleEquipped(-1);
        int[] themeOwnedUpdate = new int[] {};
        User.getLoginUser().getCustomization().setThemeOwned(themeOwnedUpdate);
        User.getLoginUser().getCustomization().setThemeEquipped(-1);

        new GetUserAsyncTask(EDIT_USER, User.getLoginUser(), null, null,null, new OnTaskComplete() {
            @Override
            public void onSuccess(Object result) {
                Toast.makeText(getActivity(), "Unlocks reset", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "Error: "+e.getMessage());
            }
        }).execute();
    }
}