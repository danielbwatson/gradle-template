package io.reactivex.lab.gateway.clients;

import com.netflix.hystrix.HystrixCollapser.CollapsedRequest;
import com.netflix.hystrix.HystrixObservableCollapser;
import com.netflix.hystrix.HystrixObservableCommand;
import io.netty.buffer.ByteBuf;
import io.reactivex.lab.gateway.clients.BookmarksCommand.Bookmark;
import io.reactivex.lab.gateway.clients.PersonalizedCatalogCommand.Video;
import io.reactivex.netty.protocol.http.sse.ServerSentEvent;
import netflix.ocelli.LoadBalancer;
import netflix.ocelli.rxnetty.HttpClientHolder;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BookmarkCommand extends HystrixObservableCollapser<Integer, Bookmark, Bookmark, Video> {

    private final Video video;
    private final LoadBalancer<HttpClientHolder<ByteBuf, ServerSentEvent>> loadBalancer;

    public BookmarkCommand(Video video, LoadBalancer<HttpClientHolder<ByteBuf, ServerSentEvent>> loadBalancer) {
        this.video = video;
        this.loadBalancer = loadBalancer;
    }

    @Override
    public Video getRequestArgument() {
        return video;
    }

    @Override
    protected HystrixObservableCommand<Bookmark> createCommand(Collection<CollapsedRequest<Bookmark, Video>> requests) {
        List<Video> videos = new ArrayList<>();
        for (CollapsedRequest<Bookmark, Video> r : requests) {
            videos.add(r.getArgument());
        }
        return new BookmarksCommand(videos, loadBalancer);
    }

    protected void onMissingResponse(CollapsedRequest<Bookmark, Video> r) {
        // set a default using setResponse or an exception like this
        r.setException(new Exception("No bookmark"));
    }

    @Override
    protected Func1<Bookmark, Integer> getBatchReturnTypeKeySelector() {
        return Bookmark::getVideoId;
    }

    @Override
    protected Func1<Video, Integer> getRequestArgumentKeySelector() {
        return Video::getId;
    }

    @Override
    protected Func1<Bookmark, Bookmark> getBatchReturnTypeToResponseTypeMapper() {
        return (b) -> b;
    }

}
