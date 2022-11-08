package ru.otus.books.dao;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.books.domain.Author;
import ru.otus.books.domain.Book;
import ru.otus.books.domain.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Repository
public class BookDaoJdbc implements BookDao {
    private final NamedParameterJdbcOperations jdbcOperations;

    public BookDaoJdbc(NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public long count() {
        var count = jdbcOperations.getJdbcOperations().queryForObject("select count(*) from books",  Long.class);
        return count == null? 0: count;
    }

    @Override
    public long insert(Book book) {
        var kh = new GeneratedKeyHolder();
        var params = new MapSqlParameterSource();
        params.addValue("name", book.getName());
        params.addValue("genre_id", book.getGenre().getId());
        params.addValue("author_id", book.getAuthor().getId());

        jdbcOperations.update("insert into books(name, genre_id, author_id) values(:name, :genre_id, :author_id)",
                 params, kh, new String[]{"id"});

        return kh.getKey().longValue();
    }

    @Override
    public Book getById(long id) {
        var params = Collections.singletonMap("id", id);

        return jdbcOperations.queryForObject("select b.id, b.name, " +
                "g.id as genre_id, g.name as genre_name,  " +
                "a.id as author_id, a.name as author_name " +
                "from books b " +
                "join genres g on g.id = b.genre_id " +
                "join authors a on a.id = b.author_id " +
                "where b.id = :id", params, new BookMapper());
    }

    @Override
    public List<Book> getAll() {
        return jdbcOperations.query("select b.id, b.name, " +
                "g.id as genre_id, g.name as genre_name,  " +
                "a.id as author_id, a.name as author_name " +
                "from books b " +
                "join genres g on g.id = b.genre_id " +
                "join authors a on a.id = b.author_id", new BookMapper());
    }

    @Override
    public void deleteById(long id) {
        var params = Collections.singletonMap("id", id);
        jdbcOperations.update("delete from books where id = :id", params);
    }

    private static class BookMapper implements RowMapper<Book> {
        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            String name = rs.getString("name");
            long genre_id = rs.getLong("genre_id");
            String genre_name = rs.getString("genre_name");
            long author_id = rs.getLong("author_id");
            String author_name = rs.getString("author_name");
            var genre = new Genre(genre_id, genre_name);
            var author = new Author(author_id, author_name);
            return new Book(id, name, author, genre );
        }
    }
}
