package ru.job4j.socialmediaapi.controller;

import org.springframework.stereotype.Component;
import ru.job4j.socialmediaapi.controller.dto.*;
import ru.job4j.socialmediaapi.repository.FriendshipRepository;
import ru.job4j.socialmediaapi.repository.OfferFriendshipRepository;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Component
public class OfferFriendshipPresenter {

    public CreateOfferFriendshipResponse presentCreateOfferResponse(
            OfferFriendshipRepository.CreateOfferFriendshipResponse source) {
        return new CreateOfferFriendshipResponse()
                .id(source.id())
                .fromUserId(source.fromUserId())
                .toUserId(source.toUserId())
                .status(CreateOfferFriendshipResponse.StatusEnum.valueOf(source.status().name()));
    }

    public AcceptOfferFriendshipResponse presentAcceptOfferFriendshipResponse(
            FriendshipRepository.CreateFriendshipResponse source) {
        return new AcceptOfferFriendshipResponse()
                .id(source.id())
                .fromUserId(source.firstUserId())
                .toUserId(source.secondUserId())
                .status(AcceptOfferFriendshipResponse.StatusEnum.ACCEPTED);
    }

    public RejectOfferFriendshipsResponse presentRejectOfferFriendshipResponse(
            OfferFriendshipRepository.FindByIdResponse source) {
        return new RejectOfferFriendshipsResponse()
                .id(source.OfferFriendshipId())
                .fromUserId(source.fromUserId())
                .toUserId(source.toUserId())
                .status(RejectOfferFriendshipsResponse.StatusEnum.REJECTED);
    }

    public FindIncomingOfferFriendshipsResponse presentFindIncomingOfferFriendshipsResponse(
            OfferFriendshipRepository.FindIncomingOfferFriendshipsResponse source,
            int page,
            int limit) {
        List<FindIncomingOfferFriendshipsResponseRequestsInner> response =
            source.offers().stream()
                    .map(offer -> new FindIncomingOfferFriendshipsResponseRequestsInner()
                            .id(UUID.randomUUID())
                            .username(offer.userId().toString())
                            .createdAt(offer.createdAt().atOffset(ZoneOffset.UTC))
                    )
                    .toList();
        long totalItems = response.size();
        int totalPages = (int) Math.ceil((double) totalItems / limit);
        if (totalPages == 0) {
            totalPages = 1;
        }

        var meta = new FindIncomingOfferFriendshipsResponseMeta()
                .totalItems(totalItems)
                .totalPages(totalPages)
                .currentPage(page);
        return new FindIncomingOfferFriendshipsResponse()
                .requests(response)
                .meta(meta);
    }

    public FindOutgoingOfferFriendshipsResponse presentFindOutgoingOfferFriendshipsResponse(
            OfferFriendshipRepository.FindOutgoingOfferFriendshipsResponse source,
            int page,
            int limit) {
        List<FindOutgoingOfferFriendshipsResponseRequestsInner> response =
                source.offers().stream()
                        .map(offer -> new FindOutgoingOfferFriendshipsResponseRequestsInner()
                                .id(UUID.randomUUID())
                                .username(offer.userId().toString())
                                .createdAt(offer.createdAt().atOffset(ZoneOffset.UTC))
                        )
                        .toList();
        long totalItems = response.size();
        int totalPages = (int) Math.ceil((double) totalItems / limit);
        if (totalPages == 0) {
            totalPages = 1;
        }

        var meta = new FindOutgoingOfferFriendshipsResponseMeta()
                .totalItems(totalItems)
                .totalPages(totalPages)
                .currentPage(page);
        return new FindOutgoingOfferFriendshipsResponse()
                .requests(response)
                .meta(meta);
    }
}