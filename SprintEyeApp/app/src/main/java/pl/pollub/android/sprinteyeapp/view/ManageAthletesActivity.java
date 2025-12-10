package pl.pollub.android.sprinteyeapp.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import pl.pollub.android.sprinteyeapp.adapters.AthleteAdapter;
import pl.pollub.android.sprinteyeapp.databinding.ActivityManageAthletesBinding;
import pl.pollub.android.sprinteyeapp.model.local.entity.Athlete;
import pl.pollub.android.sprinteyeapp.viewmodel.ManageAthletesViewModel;

public class ManageAthletesActivity extends AppCompatActivity {
    private ActivityManageAthletesBinding binding;
    private ManageAthletesViewModel viewModel;
    private AthleteAdapter adapter;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityManageAthletesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backButton.setOnClickListener(v -> {
            finish();
        });

        userId = getIntent().getLongExtra("USER_ID", -1);
        if (userId == -1){
            Toast.makeText(this, "Użytkownik niezalogowany", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViewModel();
        setupRecyclerView();
        addAthleteFabBtn();
        observeAthletes();
    }

    private void initViewModel(){
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new ManageAthletesViewModel(getApplication(), userId);
            }
        }).get(ManageAthletesViewModel.class);
    }

    private void setupRecyclerView(){
        adapter = new AthleteAdapter();
        binding.recyclerAthletes.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerAthletes.setAdapter(adapter);

        adapter.setAthleteListener(this::showDetailsAthleteDialog);
    }
    private void addAthleteFabBtn(){
        binding.fabAddAthlete.setOnClickListener(v -> {
            AddAthleteDialog dialog = new AddAthleteDialog(this, viewModel, null);
            dialog.show();
        });
    }
    private void observeAthletes(){
        viewModel.getAthletes().observe(this, athletes -> {
            if(athletes == null || athletes.isEmpty()){
                binding.recyclerAthletes.setVisibility(View.GONE);
            }else{
                binding.recyclerAthletes.setVisibility(View.VISIBLE);
                adapter.submitList(athletes);
            }
        });
    }

    private void showDetailsAthleteDialog(Athlete athlete){
        DetailsAthleteDialog dialog = new DetailsAthleteDialog(this, athlete, viewModel, athleteToEdit -> {
                    AddAthleteDialog editDialog = new AddAthleteDialog(this, viewModel, athleteToEdit);
                    editDialog.show();
                },
                        this::showDeleteConfirmation
                );
        dialog.show();
    }

    private void showDeleteConfirmation(Athlete athlete){
        new AlertDialog.Builder(this)
                .setTitle("Usuń sportowca")
                .setMessage("Potwierdź usunięcie " + athlete.nick)
                .setPositiveButton("Usuń", (dialog, which) -> {
                    viewModel.deleteAthlete(athlete);

                    Toast.makeText(this, "Usunięto sportowca", Toast.LENGTH_SHORT).show();
                    })
                .setNegativeButton("Anuluj", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
