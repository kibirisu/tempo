package com.cappielloantonio.play.ui.fragment;

import android.content.ComponentName;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaBrowser;
import androidx.media3.session.SessionToken;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cappielloantonio.play.databinding.FragmentHomeTabRadioBinding;
import com.cappielloantonio.play.interfaces.ClickCallback;
import com.cappielloantonio.play.service.MediaManager;
import com.cappielloantonio.play.service.MediaService;
import com.cappielloantonio.play.ui.activity.MainActivity;
import com.cappielloantonio.play.ui.adapter.InternetRadioStationAdapter;
import com.cappielloantonio.play.util.Constants;
import com.cappielloantonio.play.viewmodel.RadioViewModel;
import com.google.common.util.concurrent.ListenableFuture;

@UnstableApi
public class HomeTabRadioFragment extends Fragment implements ClickCallback {
    private static final String TAG = "HomeTabRadioFragment";

    private FragmentHomeTabRadioBinding bind;
    private MainActivity activity;
    private RadioViewModel radioViewModel;

    private InternetRadioStationAdapter internetRadioStationAdapter;

    private ListenableFuture<MediaBrowser> mediaBrowserListenableFuture;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();

        bind = FragmentHomeTabRadioBinding.inflate(inflater, container, false);
        View view = bind.getRoot();
        radioViewModel = new ViewModelProvider(requireActivity()).get(RadioViewModel.class);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initRadioStationView();
    }

    @Override
    public void onStart() {
        super.onStart();

        initializeMediaBrowser();
    }

    @Override
    public void onStop() {
        releaseMediaBrowser();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind = null;
    }

    private void initRadioStationView() {
        bind.internetRadioStationRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        bind.internetRadioStationRecyclerView.setHasFixedSize(true);

        internetRadioStationAdapter = new InternetRadioStationAdapter(this);
        bind.internetRadioStationRecyclerView.setAdapter(internetRadioStationAdapter);
        radioViewModel.getInternetRadioStations().observe(getViewLifecycleOwner(), internetRadioStations -> {
            if (internetRadioStations == null) {
                if (bind != null)
                    bind.internetRadioStationPlaceholder.placeholder.setVisibility(View.VISIBLE);
                if (bind != null) bind.internetRadioStationSector.setVisibility(View.GONE);
            } else {
                if (bind != null)
                    bind.internetRadioStationPlaceholder.placeholder.setVisibility(View.GONE);
                if (bind != null)
                    bind.internetRadioStationSector.setVisibility(!internetRadioStations.isEmpty() ? View.VISIBLE : View.GONE);

                internetRadioStationAdapter.setItems(internetRadioStations);
            }
        });
    }

    private void initializeMediaBrowser() {
        mediaBrowserListenableFuture = new MediaBrowser.Builder(requireContext(), new SessionToken(requireContext(), new ComponentName(requireContext(), MediaService.class))).buildAsync();
    }

    private void releaseMediaBrowser() {
        MediaBrowser.releaseFuture(mediaBrowserListenableFuture);
    }

    @Override
    public void onInternetRadioStationClick(Bundle bundle) {
        MediaManager.startRadio(mediaBrowserListenableFuture, bundle.getParcelable(Constants.INTERNET_RADIO_STATION_OBJECT));
        activity.setBottomSheetInPeek(true);
    }

    @Override
    public void onInternetRadioStationLongClick(Bundle bundle) {
        Toast.makeText(requireContext(), "Long click!", Toast.LENGTH_SHORT).show();
    }
}