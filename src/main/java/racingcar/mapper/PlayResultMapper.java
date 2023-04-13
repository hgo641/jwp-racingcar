package racingcar.mapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import racingcar.entity.PlayResultEntity;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Repository
public class PlayResultMapper {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public PlayResultMapper(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("play_result")
                .usingGeneratedKeyColumns("id");
    }

    private final RowMapper<PlayResultEntity> entityRowMapper = (resultSet, rowNum) ->
            PlayResultEntity.of(
                    resultSet.getLong("id"),
                    resultSet.getInt("trial_count"),
                    resultSet.getString("winners"),
                    resultSet.getTimestamp("created_at")
            );

    public long save(PlayResultEntity playResultEntity) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("trial_count", playResultEntity.getTrialCount());
        parameters.put("winners", playResultEntity.getWinners());
        return simpleJdbcInsert
                .usingColumns("trial_count", "winners")
                .executeAndReturnKey(parameters).longValue();
    }

    public PlayResultEntity findById(long id) {
        String sql = "SELECT * FROM play_result WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, entityRowMapper, id);
    }
}
