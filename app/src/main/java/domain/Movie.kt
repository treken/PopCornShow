package domain

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import javax.annotation.Generated

@Generated("com.robohorse.robopojogenerator")
data class Movie(

        @field:SerializedName("original_language")
        val originalLanguage: String? = null,

        @field:SerializedName("imdb_id")
        val imdbId: String? = null,

        @field:SerializedName("videos")
        val videos: Videos? = null,

        @field:SerializedName("video")
        val video: Boolean? = null,

        @field:SerializedName("title")
        val title: String? = null,

        @field:SerializedName("backdrop_path")
        val backdropPath: String? = null,

        @field:SerializedName("revenue")
        val revenue: Int? = null,

        @field:SerializedName("credits")
        val credits: Credits? = null,

        @field:SerializedName("genres")
        val genres: List<GenresItem?>? = null,

        @field:SerializedName("popularity")
        val popularity: Double? = null,

        @field:SerializedName("release_dates")
        val releaseDates: ReleaseDates? = null,

        @field:SerializedName("production_countries")
        val productionCountries: List<ProductionCountriesItem?>? = null,

        @field:SerializedName("id")
        val id: Int? = null,

        @field:SerializedName("vote_count")
        val voteCount: Int? = null,

        @field:SerializedName("budget")
        val budget: Int? = null,

        @field:SerializedName("overview")
        val overview: String? = null,

        @field:SerializedName("similar")
        val similar: Similar? = null,

        @field:SerializedName("images")
        val images: Images? = null,

        @field:SerializedName("original_title")
        val originalTitle: String? = null,

        @field:SerializedName("runtime")
        val runtime: Int? = null,

        @field:SerializedName("poster_path")
        val posterPath: String? = null,

        @field:SerializedName("spoken_languages")
        val spokenLanguages: List<SpokenLanguagesItem?>? = null,

        @field:SerializedName("production_companies")
        val productionCompanies: List<ProductionCompaniesItem?>? = null,

        @field:SerializedName("release_date")
        val releaseDate: String? = null,

        @field:SerializedName("vote_average")
        val voteAverage: Float? = null,

        @field:SerializedName("belongs_to_collection")
        val belongsToCollection: BelongsToCollection? = null,

        @field:SerializedName("tagline")
        val tagline: String? = null,

        @field:SerializedName("adult")
        val adult: Boolean? = null,

        @field:SerializedName("homepage")
        val homepage: String? = null,

        @field:SerializedName("status")
        val status: String? = null
): Serializable