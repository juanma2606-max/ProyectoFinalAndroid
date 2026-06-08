package com.example.proyectofinal.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.proyectofinal.R;
import com.example.proyectofinal.activities.AgregarHuertoActivity;
import com.example.proyectofinal.activities.AmenazaFormActivity;
import com.example.proyectofinal.activities.EditarHuertoActivity;
import com.example.proyectofinal.activities.PlantaFormActivity;
import com.example.proyectofinal.dao.AmenazaDAO;
import com.example.proyectofinal.dao.CultivoDAO;
import com.example.proyectofinal.dao.HuertoDAO;
import com.example.proyectofinal.dao.PlantaDAO;
import com.example.proyectofinal.dao.UserDAO;
import com.example.proyectofinal.modelos.Amenaza;
import com.example.proyectofinal.modelos.Huerto;
import com.example.proyectofinal.modelos.Planta;
import com.example.proyectofinal.modelos.User;
import com.example.proyectofinal.utils.SnackbarHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminFragment extends Fragment {

    private TabLayout tabLayout;
    private LinearLayout panelUsuarios, panelPlantas, panelAmenazas;

    private LinearLayout listaUsuarios, listaPlantas, listaAmenazas;
    private TextInputEditText etBuscarUsuario, etBuscarPlanta, etBuscarAmenaza;

    private UserDAO userDAO;
    private PlantaDAO plantaDAO;
    private AmenazaDAO amenazaDAO;
    private HuertoDAO huertoDAO = new HuertoDAO();
    private CultivoDAO cultivoDAO = new CultivoDAO();

    private List<User> todosUsuarios = new ArrayList<>();
    private List<Planta> todasPlantas = new ArrayList<>();
    private List<Amenaza> todasAmenazas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userDAO = new UserDAO();
        plantaDAO = new PlantaDAO();
        amenazaDAO = new AmenazaDAO();

        tabLayout = view.findViewById(R.id.tabLayoutAdmin);
        panelUsuarios = view.findViewById(R.id.panelUsuarios);
        panelPlantas = view.findViewById(R.id.panelPlantas);
        panelAmenazas = view.findViewById(R.id.panelAmenazas);

        listaUsuarios = view.findViewById(R.id.listaUsuarios);
        listaPlantas = view.findViewById(R.id.listaPlantas);
        listaAmenazas = view.findViewById(R.id.listaAmenazas);

        etBuscarUsuario = view.findViewById(R.id.etBuscarUsuario);
        etBuscarPlanta = view.findViewById(R.id.etBuscarPlanta);
        etBuscarAmenaza = view.findViewById(R.id.etBuscarAmenaza);

        // Botones nueva planta y amenaza
        MaterialButton btnNuevaPlanta = view.findViewById(R.id.btnNuevaPlanta);
        MaterialButton btnNuevaAmenaza = view.findViewById(R.id.btnNuevaAmenaza);

        btnNuevaPlanta.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), PlantaFormActivity.class);
            getActivity().startActivity(i);
        });

        btnNuevaAmenaza.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), AmenazaFormActivity.class);
            getActivity().startActivity(i);
        });

        // Tabs
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: mostrarPanel(0); break;
                    case 1: mostrarPanel(1); break;
                    case 2: mostrarPanel(2); break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Búsqueda en tiempo real
        etBuscarUsuario.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarUsuarios(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etBuscarPlanta.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarPlantas(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etBuscarAmenaza.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarAmenazas(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        mostrarPanel(0);
        cargarUsuarios();
        cargarPlantas();
        cargarAmenazas();
    }

    private void mostrarPanel(int index) {
        panelUsuarios.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        panelPlantas.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        panelAmenazas.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
    }

    // -------------------------------------------------------
    // USUARIOS
    // -------------------------------------------------------
    private void cargarUsuarios() {
        userDAO.getAllPersons(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                todosUsuarios.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    DataSnapshot profile = child.child("profile");
                    User user = profile.getValue(User.class);
                    if (user != null) {
                        user.setUid(child.getKey());
                        if (!"admin@huerting.com".equals(user.getEmail())) {
                            todosUsuarios.add(user);
                        }
                    }
                }
                filtrarUsuarios("");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                SnackbarHelper.showError(requireActivity(), "Error al cargar usuarios");
            }
        });
    }

    private void filtrarUsuarios(String query) {
        listaUsuarios.removeAllViews();
        for (User user : todosUsuarios) {
            String nombre = user.getUsername() != null ? user.getUsername().toLowerCase() : "";
            String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
            if (query.isEmpty() || nombre.contains(query.toLowerCase()) || email.contains(query.toLowerCase())) {
                listaUsuarios.addView(crearItemUsuario(user));
            }
        }
    }

    private View crearItemUsuario(User user) {
        View item = LayoutInflater.from(getContext())
                .inflate(R.layout.item_admin_usuario, listaUsuarios, false);

        TextView txtNombre = item.findViewById(R.id.txtAdminNombreUsuario);
        TextView txtEmail = item.findViewById(R.id.txtAdminEmailUsuario);
        TextView txtEstado = item.findViewById(R.id.txtAdminEstadoUsuario);
        MaterialButton btnBanear = item.findViewById(R.id.btnAdminBanear);
        MaterialButton btnEliminar = item.findViewById(R.id.btnAdminEliminarUsuario);
        MaterialButton btnVerHuertos = item.findViewById(R.id.btnAdminVerHuertos);
        LinearLayout panelHuertos = item.findViewById(R.id.panelHuertosUsuario);
        LinearLayout listaHuertosUsuario = item.findViewById(R.id.listaHuertosUsuario);

        txtNombre.setText(user.getUsername() != null ? user.getUsername() : "Sin nombre");
        txtEmail.setText(user.getEmail());

        if (user.getBaneado()) {
            txtEstado.setText("🚫 Baneado");
            txtEstado.setTextColor(0xFFB71C1C);
            btnBanear.setText("Desbanear");
            btnBanear.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF2E7D32));
        } else {
            txtEstado.setText("Activo");
            txtEstado.setTextColor(0xFF2E7D32);
            btnBanear.setText("Banear");
            btnBanear.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFFE65100));
        }

        // Botón ver huertos
        btnVerHuertos.setOnClickListener(v -> {
            if (panelHuertos.getVisibility() == View.VISIBLE) {
                panelHuertos.setVisibility(View.GONE);
                btnVerHuertos.setText("Ver huertos");
            } else {
                panelHuertos.setVisibility(View.VISIBLE);
                btnVerHuertos.setText("Ocultar huertos");
                cargarHuertosUsuario(user.getUid(), listaHuertosUsuario);
            }
        });

        // Botones banear/eliminar (los mismos de antes)
        btnBanear.setOnClickListener(v -> {
            if (user.getBaneado()) {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("¿Desbanear usuario?")
                        .setMessage("¿Seguro que quieres desbanear a " + user.getUsername() + "?")
                        .setPositiveButton("Desbanear", (d, w) ->
                                userDAO.unbanUser(user.getUid(), new UserDAO.OnOperationCallback() {
                                    @Override public void onSuccess() {
                                        SnackbarHelper.showSuccess(requireActivity(), user.getUsername() + " desbaneado");
                                    }
                                    @Override public void onError(Exception e) {
                                        SnackbarHelper.showError(requireActivity(), "Error al desbanear");
                                    }
                                }))
                        .setNegativeButton("Cancelar", null)
                        .show();
            } else {
                android.widget.EditText etMotivo = new android.widget.EditText(requireContext());
                etMotivo.setHint("Motivo del baneo");
                etMotivo.setText("Violación de términos de uso");
                etMotivo.setPadding(48, 24, 48, 24);

                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("¿Banear a " + user.getUsername() + "?")
                        .setMessage("Esta acción impedirá al usuario acceder a la aplicación.")
                        .setView(etMotivo)
                        .setPositiveButton("Banear", (d, w) -> {
                            String motivo = etMotivo.getText().toString().trim();
                            if (motivo.isEmpty()) motivo = "Violación de términos de uso";
                            final String motivoFinal = motivo;
                            userDAO.banUser(user.getUid(), motivoFinal, new UserDAO.OnOperationCallback() {
                                @Override public void onSuccess() {
                                    SnackbarHelper.showSuccess(requireActivity(), user.getUsername() + " baneado");
                                }
                                @Override public void onError(Exception e) {
                                    SnackbarHelper.showError(requireActivity(), "Error al banear");
                                }
                            });
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });

        btnEliminar.setOnClickListener(v -> {
            android.widget.EditText etConfirm = new android.widget.EditText(requireContext());
            etConfirm.setHint("Escribe ELIMINAR para confirmar");
            etConfirm.setPadding(48, 24, 48, 24);

            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("⚠️ Eliminar usuario")
                    .setMessage("Esta acción eliminará permanentemente la cuenta de " + user.getUsername() + ".")
                    .setView(etConfirm)
                    .setPositiveButton("Eliminar", null)
                    .setNegativeButton("Cancelar", null)
                    .create();

            dialog.setOnShowListener(d ->
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v2 -> {
                        if ("ELIMINAR".equals(etConfirm.getText().toString().trim())) {
                            userDAO.removePerson(user.getUid(), new UserDAO.OnOperationCallback() {
                                @Override public void onSuccess() {
                                    SnackbarHelper.showSuccess(requireActivity(), "Usuario eliminado");
                                    todosUsuarios.remove(user);
                                    filtrarUsuarios("");
                                    dialog.dismiss();
                                }
                                @Override public void onError(Exception e) {
                                    SnackbarHelper.showError(requireActivity(), "Error al eliminar");
                                }
                            });
                        } else {
                            etConfirm.setError("Escribe ELIMINAR para confirmar");
                        }
                    })
            );
            dialog.show();
        });

        return item;
    }

    private void cargarHuertosUsuario(String uid, LinearLayout contenedor) {
        contenedor.removeAllViews();

        // Botón crear huerto para este usuario
        MaterialButton btnCrearHuerto = new MaterialButton(requireContext());
        btnCrearHuerto.setText("+ Crear huerto");
        btnCrearHuerto.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFF2E7D32));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 12);
        btnCrearHuerto.setLayoutParams(params);
        btnCrearHuerto.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), AgregarHuertoActivity.class);
            i.putExtra("adminUid", uid);
            getActivity().startActivity(i);
        });
        contenedor.addView(btnCrearHuerto);

        huertoDAO.getHuertosByUid(uid, new HuertoDAO.OnHuertosLoadedCallback() {
            @Override
            public void onLoaded(List<Huerto> huertos) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (huertos.isEmpty()) {
                        TextView txt = new TextView(requireContext());
                        txt.setText("Sin huertos");
                        txt.setTextColor(0xFFA5D6A7);
                        contenedor.addView(txt);
                        return;
                    }
                    for (Huerto huerto : huertos) {
                        contenedor.addView(crearItemHuerto(uid, huerto, contenedor));
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                SnackbarHelper.showError(requireActivity(), "Error al cargar huertos");
            }
        });
    }

    private View crearItemHuerto(String uid, Huerto huerto, LinearLayout contenedor) {
        com.google.android.material.card.MaterialCardView card =
                new com.google.android.material.card.MaterialCardView(requireContext());
        card.setCardBackgroundColor(0xFF1B3A1B);
        card.setRadius(16f);
        card.setUseCompatPadding(true);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 24, 32, 24);

        TextView txtNombre = new TextView(requireContext());
        txtNombre.setText("🌱 " + huerto.getNombre());
        txtNombre.setTextColor(0xFFFFFFFF);
        txtNombre.setTextSize(14);
        txtNombre.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(txtNombre);

        TextView txtUbicacion = new TextView(requireContext());
        txtUbicacion.setText("📍 " + huerto.getUbicacion());
        txtUbicacion.setTextColor(0xFFA5D6A7);
        txtUbicacion.setTextSize(12);
        layout.addView(txtUbicacion);

        LinearLayout botonesHuerto = new LinearLayout(requireContext());
        botonesHuerto.setOrientation(LinearLayout.HORIZONTAL);
        botonesHuerto.setPadding(0, 16, 0, 0);

        MaterialButton btnEditar = new MaterialButton(requireContext());
        btnEditar.setText("Editar");
        btnEditar.setTextSize(11);
        btnEditar.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFFFF9800));
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        btnParams.setMarginEnd(8);
        btnEditar.setLayoutParams(btnParams);
        btnEditar.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), EditarHuertoActivity.class);
            i.putExtra("huertoId", huerto.getId());
            i.putExtra("adminUid", uid);
            getActivity().startActivity(i);
        });

        MaterialButton btnEliminar = new MaterialButton(requireContext());
        btnEliminar.setText("Eliminar");
        btnEliminar.setTextSize(11);
        btnEliminar.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFFB71C1C));
        btnEliminar.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        btnEliminar.setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("¿Eliminar huerto?")
                        .setMessage("Se eliminarán también todos sus cultivos.")
                        .setPositiveButton("Eliminar", (d, w) ->
                                huertoDAO.removeHuerto(huerto.getId(), new HuertoDAO.OnCompleteCallback() {
                                    @Override public void onSuccess() {
                                        SnackbarHelper.showSuccess(requireActivity(), "Huerto eliminado");
                                        contenedor.removeView(card);
                                    }
                                    @Override public void onError(String msg) {
                                        SnackbarHelper.showError(requireActivity(), "Error: " + msg);
                                    }
                                }))
                        .setNegativeButton("Cancelar", null)
                        .show()
        );

        botonesHuerto.addView(btnEditar);
        botonesHuerto.addView(btnEliminar);
        layout.addView(botonesHuerto);
        card.addView(layout);
        return card;
    }

    // -------------------------------------------------------
    // PLANTAS
    // -------------------------------------------------------
    private void cargarPlantas() {
        plantaDAO.getAllPlantasOnce(new PlantaDAO.OnPlantasLoadedCallback() {
            @Override
            public void onLoaded(List<Planta> plantas) {
                todasPlantas = plantas;
                filtrarPlantas("");
            }
            @Override
            public void onError(Exception e) {
                SnackbarHelper.showError(requireActivity(), "Error al cargar plantas");
            }
        });
    }

    private void filtrarPlantas(String query) {
        listaPlantas.removeAllViews();
        for (Planta planta : todasPlantas) {
            String nombre = planta.getNombre() != null ? planta.getNombre().toLowerCase() : "";
            if (query.isEmpty() || nombre.contains(query.toLowerCase())) {
                listaPlantas.addView(crearItemPlanta(planta));
            }
        }
    }

    private View crearItemPlanta(Planta planta) {
        View item = LayoutInflater.from(getContext())
                .inflate(R.layout.item_admin_planta, listaPlantas, false);

        TextView txtNombre = item.findViewById(R.id.txtAdminNombrePlanta);
        TextView txtTipo = item.findViewById(R.id.txtAdminTipoPlanta);
        MaterialButton btnEditar = item.findViewById(R.id.btnAdminEditarPlanta);
        MaterialButton btnEliminar = item.findViewById(R.id.btnAdminEliminarPlanta);

        txtNombre.setText(planta.getNombre());
        txtTipo.setText(planta.getTipo() != null ? planta.getTipo() : "");

        btnEditar.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), PlantaFormActivity.class);
            i.putExtra("plantaId", planta.getId());
            getActivity().startActivity(i);
        });

        btnEliminar.setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("¿Eliminar planta?")
                        .setMessage("¿Seguro que quieres eliminar " + planta.getNombre() + "?")
                        .setPositiveButton("Eliminar", (d, w) ->
                                plantaDAO.removePlanta(planta.getId(), new PlantaDAO.OnOperationCallback() {
                                    @Override public void onSuccess() {
                                        SnackbarHelper.showSuccess(requireActivity(), "Planta eliminada");
                                        todasPlantas.remove(planta);
                                        filtrarPlantas(etBuscarPlanta.getText() != null
                                                ? etBuscarPlanta.getText().toString() : "");
                                    }
                                    @Override public void onError(Exception e) {
                                        SnackbarHelper.showError(requireActivity(), "Error al eliminar");
                                    }
                                }))
                        .setNegativeButton("Cancelar", null)
                        .show()
        );

        return item;
    }

    // -------------------------------------------------------
    // AMENAZAS
    // -------------------------------------------------------
    private void cargarAmenazas() {
        amenazaDAO.getAllAmenazasOnce(new AmenazaDAO.OnAmenazasLoadedCallback() {
            @Override
            public void onLoaded(List<Amenaza> amenazas) {
                todasAmenazas = amenazas;
                filtrarAmenazas("");
            }
            @Override
            public void onError(Exception e) {
                SnackbarHelper.showError(requireActivity(), "Error al cargar amenazas");
            }
        });
    }

    private void filtrarAmenazas(String query) {
        listaAmenazas.removeAllViews();
        for (Amenaza amenaza : todasAmenazas) {
            String nombre = amenaza.getNombre() != null ? amenaza.getNombre().toLowerCase() : "";
            if (query.isEmpty() || nombre.contains(query.toLowerCase())) {
                listaAmenazas.addView(crearItemAmenaza(amenaza));
            }
        }
    }

    private View crearItemAmenaza(Amenaza amenaza) {
        View item = LayoutInflater.from(getContext())
                .inflate(R.layout.item_admin_amenaza, listaAmenazas, false);

        TextView txtNombre = item.findViewById(R.id.txtAdminNombreAmenaza);
        TextView txtTipo = item.findViewById(R.id.txtAdminTipoAmenaza);
        MaterialButton btnEditar = item.findViewById(R.id.btnAdminEditarAmenaza);
        MaterialButton btnEliminar = item.findViewById(R.id.btnAdminEliminarAmenaza);

        txtNombre.setText(amenaza.getNombre());
        txtTipo.setText(amenaza.getTipo() != null ? amenaza.getTipo() : "");

        btnEditar.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), AmenazaFormActivity.class);
            i.putExtra("amenazaId", amenaza.getId());
            getActivity().startActivity(i);
        });

        btnEliminar.setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("¿Eliminar amenaza?")
                        .setMessage("¿Seguro que quieres eliminar " + amenaza.getNombre() + "?")
                        .setPositiveButton("Eliminar", (d, w) ->
                                amenazaDAO.removeAmenaza(amenaza.getId(), new AmenazaDAO.OnOperationCallback() {
                                    @Override public void onSuccess() {
                                        SnackbarHelper.showSuccess(requireActivity(), "Amenaza eliminada");
                                        todasAmenazas.remove(amenaza);
                                        filtrarAmenazas(etBuscarAmenaza.getText() != null
                                                ? etBuscarAmenaza.getText().toString() : "");
                                    }
                                    @Override public void onError(Exception e) {
                                        SnackbarHelper.showError(requireActivity(), "Error al eliminar");
                                    }
                                }))
                        .setNegativeButton("Cancelar", null)
                        .show()
        );

        return item;
    }
}