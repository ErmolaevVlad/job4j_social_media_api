package ru.job4j.socialmediaapi.controller;

import org.springframework.stereotype.Component;
import ru.job4j.socialmediaapi.controller.dto.CreateOfferFriendshipResponse;
import ru.job4j.socialmediaapi.repository.OfferFriendshipRepository;

@Component
public class OfferFriendshipPresenter {

    public CreateOfferFriendshipResponse present(
            OfferFriendshipRepository.CreateOfferFriendshipResponse source) {
        return new CreateOfferFriendshipResponse()
                .id(source.id())
                .fromUserId(source.fromUserId())
                .toUserId(source.toUserId())
                .status(CreateOfferFriendshipResponse.StatusEnum.valueOf(source.status().name()));
    }
}
