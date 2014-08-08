package org.magnum.dataup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class VideoController {

    private static final AtomicLong _currentId = new AtomicLong(0L);
	private static final Map<Long,Video> _videos = new HashMap<Long, Video>();
	private VideoFileManager _videoDataMgr;
	
	public VideoController() throws IOException{
		_videoDataMgr = VideoFileManager.get();
	}
	
	@RequestMapping(value="/video", method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getAllVideos(){
		return _videos.values();
	}
	
	@RequestMapping(value="/video", method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video video){
		video = checkAndSetId(video);
		long videoId = video.getId();
		video.setDataUrl(getDataUrl(videoId));
		_videos.put(videoId, video);
		return video;
	}
	
    @RequestMapping(value="/video/{id}/data", method=RequestMethod.GET)
    public void getVideoFile(
               @PathVariable("id") long id, 
               HttpServletResponse response) throws IOException {
    	if(!_videos.containsKey(id))
    		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    	else {
	    	Video video = _videos.get(id);
	    	_videoDataMgr.copyVideoData(video, response.getOutputStream());
    	}
    }
	
	@RequestMapping(value="/video/{id}/data", method=RequestMethod.POST)
	public @ResponseBody VideoStatus uploadVideoFile(
			@PathVariable long id, 
			final @RequestParam("data") MultipartFile data, 
			HttpServletResponse response) throws IOException {
    	if(!_videos.containsKey(id))
    		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    	else {
    		Video video = _videos.get(id);
    		_videoDataMgr.saveVideoData(video, data.getInputStream());
    	}
		return new VideoStatus(VideoState.READY);
	}
	
	private Video checkAndSetId(Video entity) {
		if(entity.getId() == 0){
			entity.setId(_currentId.incrementAndGet());
		}
		return entity;
	}
	
    private String getDataUrl(long videoId){
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }

 	private String getUrlBaseForLocalServer() {
	   HttpServletRequest request = 
	       ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	   String base = 
	      "http://"+request.getServerName() 
	      + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
	   return base;
	}
}
