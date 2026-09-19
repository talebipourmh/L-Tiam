package com.abania.dragmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.util.List;

public class DonationDialog extends DialogFragment {
    
    private List<Medicine> medicineList;
    private OnDonationConfirmListener listener;
    private Medicine selectedMedicine;
    private int quantity = 1;

    // اینترفیس برای بازگشت نتیجه
    public interface OnDonationConfirmListener {
        void onDonationConfirmed(Medicine medicine, int quantity);
    }

    // Constructor
    public DonationDialog(List<Medicine> medicineList) {
        this.medicineList = medicineList;
    }

    // تنظیم listener
    public void setOnDonationConfirmListener(OnDonationConfirmListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_donation, null);

        ListView listView = view.findViewById(R.id.list_medicines);
        NumberPicker numberPicker = view.findViewById(R.id.number_picker_quantity);

        // تنظیم NumberPicker برای تعداد
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(100);
        numberPicker.setValue(1);
        numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> quantity = newVal);

        // ایجاد لیست اسامی داروها برای نمایش
        String[] medicineNames = new String[medicineList.size()];
        for (int i = 0; i < medicineList.size(); i++) {
            medicineNames[i] = medicineList.get(i).getName() + 
                               " (موجودی: " + medicineList.get(i).getQuantity() + ")";
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), 
                android.R.layout.simple_list_item_single_choice, medicineNames);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // انتخاب آیتم
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            selectedMedicine = medicineList.get(position);
            // محدود کردن حداکثر تعداد به موجودی
            numberPicker.setMaxValue(Math.max(1, selectedMedicine.getQuantity()));
            Toast.makeText(getActivity(), "داروی " + selectedMedicine.getName() + " انتخاب شد", Toast.LENGTH_SHORT).show();
        });

        builder.setView(view)
                .setTitle("🎯 انتخاب دارو برای اهدا")
                .setPositiveButton("📤 ارسال درخواست", (dialog, which) -> {
                    if (selectedMedicine != null) {
                        if (quantity > selectedMedicine.getQuantity()) {
                            Toast.makeText(getActivity(), 
                                "❌ تعداد درخواستی بیشتر از موجودی است!", 
                                Toast.LENGTH_SHORT).show();
                            return;
                        }
                        listener.onDonationConfirmed(selectedMedicine, quantity);
                    } else {
                        Toast.makeText(getActivity(), "❌ لطفاً یک دارو انتخاب کنید", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("❌ لغو", null);

        return builder.create();
    }
}