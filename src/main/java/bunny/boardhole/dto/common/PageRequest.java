package bunny.boardhole.dto.common;

import lombok.Data;

@Data
public class PageRequest {
    private int page = 0;
    private int size = 10;
    private String search;
    private String sortBy = "id";
    private String sortDirection = "desc";
    
    public int getOffset() {
        return page * size;
    }
}