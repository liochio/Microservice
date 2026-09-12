# Package com.liochio.music (Controller, DTO, Entity, Repository, Service)

## Controller (`com.liochio.music.controller`)
- `MusicController`: Tiếp nhận yêu cầu tạo/sửa/xóa Nghệ sĩ, Album, Bài hát, Playlist, tăng lượt nghe và tra cứu Top Trending.

## DTO (`com.liochio.music.dto`)
- `SongCreateRequest`, `SongResponse`: Quản lý upload bài hát và link stream.
- `ArtistResponse`, `AlbumResponse`: Thông tin nghệ sĩ và album.

## Entity (`com.liochio.music.entity`)
- `ArtistEntity`, `AlbumEntity`, `SongEntity`, `PlaylistEntity`, `MusicGenreEntity`, `TrackReviewEntity`.

## Repository & Service (`com.liochio.music.repository`, `com.liochio.music.service`)
- `MusicService`: Xử lý logic nghiệp vụ stream nhạc, đồng bộ lời bài hát LRC và thống kê lượt nghe.
