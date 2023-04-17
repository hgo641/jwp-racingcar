package racingcar.service;

import org.springframework.stereotype.Service;
import racingcar.dao.CarResultDao;
import racingcar.dao.PlayResultDao;
import racingcar.domain.*;
import racingcar.dto.request.CarGameRequest;
import racingcar.dto.response.CarGameResponse;
import racingcar.dto.response.CarResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RacingGameService {

    public static final String WINNER_DELIMITER = ",";
    private final CarResultDao carResultDao;

    private final PlayResultDao playResultDao;

    public RacingGameService(CarResultDao carResultDao, PlayResultDao playResultDao) {
        this.carResultDao = carResultDao;
        this.playResultDao = playResultDao;
    }

    public CarGameResponse play(CarGameRequest carGameRequest) {
        RacingGame racingGame = getRacingGame(carGameRequest);
        int tryCount = racingGame.getTryCount();
        progress(racingGame);
        Cars cars = racingGame.getCars();
        String winners = String.join(WINNER_DELIMITER, racingGame.decideWinners());

        int playResultId = savePlayResult(tryCount, racingGame, winners);
        saveCarResult(cars, playResultId);
        List<CarResponse> carResponses = getCarResponses(cars);

        return new CarGameResponse(winners, carResponses);
    }

    private static RacingGame getRacingGame(CarGameRequest carGameRequest) {
        List<String> names = Arrays.stream(carGameRequest.getNames().split(",")).collect(Collectors.toList());
        RacingGame racingGame = new RacingGame(new CarRandomNumberGenerator(), new Cars(names), carGameRequest.getCount());
        return racingGame;
    }

    private static List<CarResponse> getCarResponses(Cars cars) {
        return cars.getUnmodifiableCars()
                .stream()
                .map(CarResponse::new)
                .collect(Collectors.toList());
    }

    private int savePlayResult(int tryCount, RacingGame racingGame, String winners) {
        return playResultDao.save(PlayResult.of(tryCount, winners, racingGame.getCreatedAt()));
    }

    private void saveCarResult(Cars cars, int playResultId) {
        cars.getUnmodifiableCars()
                .stream()
                .map(car -> CarResult.of(playResultId, car.getName(), car.getPosition()))
                .forEach(carResultDao::save);
    }

    private void progress(RacingGame racingGame) {
        while (!racingGame.isEnd()) {
            racingGame.play();
        }
    }

    public List<CarGameResponse> findAllCarGame() {
        List<CarGameResponse> carGameResponses = new ArrayList<>();
        for (Integer playResultId : playResultDao.findAllId()) {
            PlayResult playResult = playResultDao.findById(playResultId);
            List<CarResult> carResults = carResultDao.findAllByPlayResultId(playResultId);
            List<CarResponse> carResponses = carResults.stream()
                    .map(CarResponse::new)
                    .collect(Collectors.toList());
            carGameResponses.add(new CarGameResponse(playResult.getWinners(), carResponses));
        }
        return carGameResponses;
    }
}
