package de.quinscape.domainql.util;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Contains paginated [T] rows.
 *
 * @param <T> payload type
 */
public class Paged<T>
{
    private List<T> rows;

    private int rowCount;


    public Paged()
    {
        this(null, -1);
    }


    public Paged(List<T> rows, int rowCount)
    {
        this.rows = rows;
        this.rowCount = rowCount;
    }


    /**
     * List of [T] rows.
     * @return
     */
    @NotNull
    public List<T> getRows()
    {
        return rows;
    }


    public void setRows(List<T> rows)
    {
        this.rows = rows;
    }


    /**
     * Row count available.
     *
     * @return
     */
    @NotNull
    public int getRowCount()
    {
        return rowCount;
    }


    public void setRowCount(int rowCount)
    {
        this.rowCount = rowCount;
    }
}
