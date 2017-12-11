package de.kiwiwings.poi.ssview;

import java.util.List;
import java.util.Map;

public interface StructuredSource {
	List<String> getStructuredHeader();
	List<Map<String,Object>> getStructuredData();
}
