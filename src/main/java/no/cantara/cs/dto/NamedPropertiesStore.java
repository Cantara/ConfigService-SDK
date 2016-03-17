package no.cantara.cs.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Consider merge/reuse later, https://github.com/constretto/constretto-core/blob/master/constretto-core/src/main/java/org/constretto/internal/store/PropertiesStore.java
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-08-07.
 */
public class NamedPropertiesStore implements Serializable {

	private static final long serialVersionUID = 755323393221223803L;
	
	public String fileName;
    public final LinkedHashMap<String, String> properties;

    public NamedPropertiesStore(LinkedHashMap<String, String> properties, String fileName) {
        this.properties = properties;
        this.fileName = fileName;
    }

    public NamedPropertiesStore() {
        this.properties = new LinkedHashMap<>();
    }

    @Override
    public String toString() {
        return "NamedPropertiesStore{" +
                "fileName='" + fileName + '\'' +
                ", properties.size=" + properties.size() +
                '}';
    }
}
