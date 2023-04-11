package racingcar.dto.response;

import racingcar.domain.Car;

public class CarDto {
    private final String name;
    private final int position;

    public CarDto(final Car car) {
        this.name = car.getName();
        this.position = car.getPosition();
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }
}
