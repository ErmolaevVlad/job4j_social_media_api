package ru.job4j.socialmediaapi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.socialmediaapi.controller.dto.*;
import ru.job4j.socialmediaapi.exception.DomainException;
import ru.job4j.socialmediaapi.exception.OfferFriendshipAlreadyProcessedException;
import ru.job4j.socialmediaapi.repository.FriendshipRepository;
import ru.job4j.socialmediaapi.repository.FriendshipStatus;
import ru.job4j.socialmediaapi.repository.OfferFriendshipRepository;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class FriendshipService {
    private final OfferFriendshipRepository offerFriendshipRepository;
    private final FriendshipRepository friendshipRepository;

    public FriendshipService(OfferFriendshipRepository offerFriendshipRepository, FriendshipRepository friendshipRepository) {
        this.offerFriendshipRepository = offerFriendshipRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public OfferFriendshipRepository.CreateOfferFriendshipResponse createOfferFriendship(CreateOfferFriendshipRequest apiRequest)
            throws DomainException {

        OfferFriendshipRepository.CreateOfferFriendshipRequest createOfferFriendshipRequest =
                new OfferFriendshipRepository.CreateOfferFriendshipRequest(
                    apiRequest.getFromUserId(),
                    apiRequest.getToUserId(),
                    FriendshipStatus.PENDING,
                    Instant.now(),
                    Instant.now()
        );

        return offerFriendshipRepository.createOfferFriendship(createOfferFriendshipRequest);
    }

    @Transactional(rollbackFor = Exception.class)
    public AcceptOfferFriendshipResponse acceptOfferFriendship(AcceptOfferFriendshipRequest apiRequest) throws DomainException {
        var offerFriendship = offerFriendshipRepository.findById(
                new OfferFriendshipRepository.FindByIdRequest(apiRequest.getRequestId()));
        if (offerFriendship.status() != FriendshipStatus.PENDING) {
            throw new OfferFriendshipAlreadyProcessedException("Friendship request has already been processed");
        }

        FriendshipRepository.CreateFriendshipRequest createFriendshipRequest =
                new FriendshipRepository.CreateFriendshipRequest(
                        offerFriendship.fromUserId(),
                        offerFriendship.toUserId(),
                        offerFriendship.OfferFriendshipId(),
                        Instant.now()
                );
        FriendshipRepository.CreateFriendshipResponse responseService =
                friendshipRepository.createFriendship(createFriendshipRequest);
        offerFriendshipRepository.updateStatusOfferFriendship(offerFriendship.OfferFriendshipId(),
                FriendshipStatus.ACCEPTED);

        return new AcceptOfferFriendshipResponse(
                responseService.id(),
                responseService.firstUserId(),
                responseService.secondUserId(),
                AcceptOfferFriendshipResponse.StatusEnum.ACCEPTED
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public RejectOfferFriendshipsResponse rejectOfferFriendship(RejectOfferFriendshipsRequest apiRequest) throws DomainException {
        var offerFriendship = offerFriendshipRepository.findById(
                new OfferFriendshipRepository.FindByIdRequest(apiRequest.getRequestId()));
        if (offerFriendship.status() != FriendshipStatus.PENDING) {
            throw new OfferFriendshipAlreadyProcessedException("Friendship request has already been processed");
        }

        offerFriendshipRepository.updateStatusOfferFriendship(offerFriendship.OfferFriendshipId(),
                FriendshipStatus.REJECTED);

        return new RejectOfferFriendshipsResponse(
                offerFriendship.OfferFriendshipId(),
                offerFriendship.fromUserId(),
                offerFriendship.toUserId(),
                RejectOfferFriendshipsResponse.StatusEnum.REJECTED
        );
    }
}
