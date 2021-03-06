package r.demo.graphql.core;

import com.google.cloud.translate.v3.Translation;
import graphql.schema.DataFetcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import r.demo.graphql.annotation.Gql;
import r.demo.graphql.annotation.GqlDataFetcher;
import r.demo.graphql.annotation.GqlType;
import r.demo.graphql.config.GoogleTranslationClient;
import r.demo.graphql.domain.documents.video.Video;
import r.demo.graphql.domain.documents.video.VideoRepository;
import r.demo.graphql.response.BubbleResponse;
import r.demo.graphql.types.Bubble;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Gql
@Service
public class ELKDataFetcher {
    private final GoogleTranslationClient googleTranslationClient;
    private final VideoRepository videoRepository;

    public ELKDataFetcher(GoogleTranslationClient googleTranslationClient, VideoRepository videoRepository) {
        this.googleTranslationClient = googleTranslationClient;
        this.videoRepository = videoRepository;
    }

    // 한글 검색어에 대해 영어로 변환 (Data 가 영어 text 로 구성돼있음.)
    // 이후 영어 검색어에 대한 search query 수행
    @GqlDataFetcher(type = GqlType.QUERY)
    public DataFetcher<?> ocean() {
        return environment -> {
            String keyword = environment.getArgument("keyword"),
                    dFilter = environment.getArgument("dFilter");
            try {
                final LinkedHashMap<String, Object> req = environment.getArgument("pr");
                int page = Integer.parseInt(req.get("page").toString()),
                        renderItem = Integer.parseInt(req.get("renderItem").toString());
                PageRequest pageRequest = PageRequest.of(page - 1, renderItem);

                if (keyword == null) keyword = "";
                else {
                    String translatedEng = "";
                    try {
                        // keyword to english
                        translatedEng = googleTranslationClient.getTranslatedParagraphs(keyword, "en")
                                .stream().map(Translation::getTranslatedText).collect(Collectors.joining(" "));
                    } catch (RuntimeException ignored) {
                    } finally {
                        keyword = translatedEng;
                    }
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Page<Video> videos = dFilter == null ?
                        "".equals(keyword) ? videoRepository.findAll(pageRequest)
                                : videoRepository.findAllByCaptionsInOrTitleIn(keyword, keyword, pageRequest)
                        :
                        "".equals(keyword) ? videoRepository.findAllByCreatedBetween(sdf.parse(dFilter.concat(" 00:00:00")), sdf.parse(dFilter.concat(" 23:59:59")), pageRequest)
                                : videoRepository.findAllByCaptionsInAndCreatedBetweenOrTitleInAndCreatedBetween(
                                keyword, sdf.parse(dFilter.concat(" 00:00:00")), sdf.parse(dFilter.concat(" 23:59:59")),
                                        keyword, sdf.parse(dFilter.concat(" 00:00:00")), sdf.parse(dFilter.concat(" 23:59:59")), pageRequest);
                return new BubbleResponse(videos.stream().map(Bubble::new).collect(Collectors.toList()), videos.getTotalPages());
            } catch (Exception e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        };
    }

    @GqlDataFetcher(type = GqlType.QUERY)
    public DataFetcher<?> bubble() {
        return environment -> {
            String id = environment.getArgument("id");
            try {
                return videoRepository.findById(id);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        };
    }
}
