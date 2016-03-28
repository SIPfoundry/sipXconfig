package org.sipfoundry.sipxconfig.site.e911;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry.form.IPropertySelectionModel;
import org.sipfoundry.sipxconfig.e911.E911Location;

public class E911LocationSelectionModel implements IPropertySelectionModel {
	public static String E911_LOCATION_SETTINGS = "e911/location"; 
	
    private List<E911Location> m_locations = new ArrayList<E911Location>();

    public E911LocationSelectionModel(List<E911Location> locations) {
    	m_locations = locations;
    	m_locations.add(0, createDefaultLocation()); //Push none selected location
    }

    @Override
    public String getLabel(int index) {
        return m_locations.get(index).getLocation();
    }

    @Override
    public Object getOption(int index) {
        return m_locations.get(index).getId();
    }

    @Override
    public int getOptionCount() {
        return m_locations.size();
    }

    @Override
    public String getValue(int index) {
        return Integer.toString(m_locations.get(index).getId());
    }

    @Override
    public boolean isDisabled(int index) {
        return false;
    }

    @Override
    public Object translateValue(String value) {
    	return Integer.parseInt(value);
    }
    
    private E911Location createDefaultLocation()
    {
    	E911Location location = new E911Location();
    	location.setLocation("None");
    	return location;
    }

}