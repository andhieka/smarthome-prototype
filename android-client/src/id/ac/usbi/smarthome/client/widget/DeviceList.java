package id.ac.usbi.smarthome.client.widget;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import id.ac.usbi.smarthome.client.R;
import org.w3c.dom.Text;

/**
 * Created by andhieka on 30/5/14.
 */
public class DeviceList extends ArrayAdapter<String> {
    private final Activity _context;
    private final String[] _device_names;
    private final Integer[] _device_images;

    public DeviceList(Activity context, String[] device_names, Integer[] device_images) {
        super(context, R.layout.appliance_list_single, device_names);
        this._context = context;
        this._device_names = device_names;
        this._device_images = device_images;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = _context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.appliance_list_single, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(_device_names[position]);
        imageView.setImageResource(_device_images[position]);
        return rowView;
    }
}
