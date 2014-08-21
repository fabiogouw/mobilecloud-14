/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.magnum.mobilecloud.video;

import java.security.Principal;
import java.util.Collection;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;

import com.google.common.collect.Lists;

@Controller
public class VideoController {
	
	@Autowired
	private VideoRepository _videoRepository;

	@RequestMapping(value="/video", method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList(){
		return Lists.newArrayList(_videoRepository.findAll());
	}
	
	@RequestMapping(value="/video", method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video video) {
		video.setLikes(0);
		video = _videoRepository.save(video);
		return video;
	}

	@RequestMapping(value="/video/{id}", method=RequestMethod.GET)
	public @ResponseBody Video getVideoById(@PathVariable("id") long id
			, HttpServletResponse response) {
		Video video = _videoRepository.findOne(id);
		if(video == null)
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return video;
	}

	@RequestMapping(value="/video/{id}/like", method=RequestMethod.POST)
	public void likeVideo(@PathVariable("id") long id
			, Principal principal
			, HttpServletResponse response) {		
		Video video = _videoRepository.findOne(id);
		if(video == null)
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		else {
			String username = principal.getName();
			boolean newLike = video.like(username);
			if(!newLike)
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			_videoRepository.save(video);
		}
	}

	@RequestMapping(value="/video/{id}/unlike", method=RequestMethod.POST)
	public void unlikeVideo(@PathVariable("id") long id
			, Principal principal
			, HttpServletResponse response) {
		Video video = _videoRepository.findOne(id);
		if(video == null)
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		else {
			String username = principal.getName();
			video.unlike(username);
			_videoRepository.save(video);
		}
	}
	
	@RequestMapping(value="/video/{id}/likedby", method=RequestMethod.GET)
	public @ResponseBody Collection<String> getUsersWhoLikedVideo(@PathVariable("id") long id
			, HttpServletResponse response) {
		Video video = _videoRepository.findOne(id);
		if(video == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return Lists.newArrayList();
		}
		else {
			return video.getUsersWhoLiked();
		}
	}
	
	@RequestMapping(value="/video/search/findByName", method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByTitle(@RequestParam("title") String title)
	{
		return _videoRepository.findByName(title);
	}

	@RequestMapping(value="/video/search/findByDurationLessThan", method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(@RequestParam("duration") long duration) {
		return _videoRepository.findByDurationLessThan(duration);
	}
	
}
