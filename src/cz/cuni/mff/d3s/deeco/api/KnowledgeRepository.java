package cz.cuni.mff.d3s.deeco.api;

import java.util.List;
import java.util.Map;

public interface KnowledgeRepository {
	Map<String, Object> get(List<String> ids);
	void update(Map<String, Object> changeset);
}
