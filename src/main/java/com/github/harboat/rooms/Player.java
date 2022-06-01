package com.github.harboat.rooms;

import lombok.*;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @ToString
@Builder
public class Player {
    private Boolean ready;
    private Boolean fleetSet;
}
