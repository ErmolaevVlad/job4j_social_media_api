package ru.job4j.socialmediaapi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.socialmediaapi.controller.dto.*;
import ru.job4j.socialmediaapi.exception.DomainException;
import ru.job4j.socialmediaapi.exception.DuplicateFriendshipException;
import ru.job4j.socialmediaapi.exception.OfferFriendshipAlreadyProcessedException;
import ru.job4j.socialmediaapi.repository.FriendshipRepository;
import ru.job4j.socialmediaapi.repository.FriendshipStatus;
import ru.job4j.socialmediaapi.repository.OfferFriendshipRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OfferFriendshipService {
    private final OfferFriendshipRepository offerFriendshipRepository;
    private final FriendshipRepository friendshipRepository;

    public OfferFriendshipService(OfferFriendshipRepository offerFriendshipRepository, FriendshipRepository friendshipRepository) {
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

    @Transactional(
            rollbackFor = Exception.class,
            noRollbackFor = DuplicateFriendshipException.class)
    public FriendshipRepository.CreateFriendshipResponse acceptOfferFriendship(AcceptOfferFriendshipRequest apiRequest) throws DomainException {
        var offerFriendship = offerFriendshipRepository.findById(
                new OfferFriendshipRepository.FindByIdRequest(apiRequest.getRequestId()));
        if (offerFriendship.status() != FriendshipStatus.PENDING) {
            throw new OfferFriendshipAlreadyProcessedException("Friendship request has already been processed");
        }
        UUID user1 = offerFriendship.fromUserId();
        UUID user2 = offerFriendship.toUserId();

        boolean isUser1First = user1.toString().compareTo(user2.toString()) < 0;
        UUID firstUserId = isUser1First ? user1 : user2;
        UUID secondUserId = isUser1First ? user2 : user1;

        FriendshipRepository.CreateFriendshipRequest createFriendshipRequest =
                new FriendshipRepository.CreateFriendshipRequest(
                        firstUserId,
                        secondUserId,
                        offerFriendship.OfferFriendshipId(),
                        Instant.now()
                );
        var accept = offerFriendshipRepository.updateStatusOfferFriendship(offerFriendship.OfferFriendshipId(),
                FriendshipStatus.ACCEPTED);
        if (!accept) {
            throw new OfferFriendshipAlreadyProcessedException("Friendship request has already been processed");
        }
        return friendshipRepository.createFriendship(createFriendshipRequest);
    }

    @Transactional(rollbackFor = Exception.class)
    public OfferFriendshipRepository.FindByIdResponse rejectOfferFriendship(RejectOfferFriendshipsRequest apiRequest) throws DomainException {
        var offerFriendship = offerFriendshipRepository.findById(
                new OfferFriendshipRepository.FindByIdRequest(apiRequest.getRequestId()));
        if (offerFriendship.status() != FriendshipStatus.PENDING) {
            throw new OfferFriendshipAlreadyProcessedException("Friendship request has already been processed");
        }

        offerFriendshipRepository.updateStatusOfferFriendship(offerFriendship.OfferFriendshipId(),
                FriendshipStatus.REJECTED);

        return offerFriendship;
    }

    public OfferFriendshipRepository.FindIncomingOfferFriendshipsResponse findIncomingOfferFriendship(UUID userId) throws DomainException {
        OfferFriendshipRepository.FindOfferFriendshipsRequest findOfferFriendshipsRequest =
                new OfferFriendshipRepository.FindOfferFriendshipsRequest(
                        userId
                );
        return offerFriendshipRepository.findIncomingOfferFriendships(findOfferFriendshipsRequest);
    }

    public OfferFriendshipRepository.FindOutgoingOfferFriendshipsResponse findOutgoingOfferFriendship(UUID userId) throws DomainException {
        OfferFriendshipRepository.FindOfferFriendshipsRequest findOfferFriendshipsRequest =
                new OfferFriendshipRepository.FindOfferFriendshipsRequest(
                        userId
                );
        return offerFriendshipRepository.findOutgoingOfferFriendships(findOfferFriendshipsRequest);
    }
}