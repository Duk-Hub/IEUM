CREATE TABLE member
(
	id            BIGINT       NOT NULL AUTO_INCREMENT,
	auth_type     VARCHAR(20)  NOT NULL,
	username      VARCHAR(16)  NULL,
	password      VARCHAR(255) NULL,
	phone         VARCHAR(20)  NOT NULL,
	provider      VARCHAR(20)  NULL,
	provider_id   VARCHAR(100) NULL,
	nickname      VARCHAR(16)  NOT NULL,
	role          VARCHAR(20)  NOT NULL,
	score         INT          NOT NULL DEFAULT 0,
	grade         VARCHAR(20)  NOT NULL,
	created_at    DATETIME     NOT NULL,
	updated_at    DATETIME     NOT NULL,
	is_deleted    TINYINT(1)   NOT NULL DEFAULT 0,
	version       BIGINT       NOT NULL DEFAULT 0,

	PRIMARY KEY (id),

	CONSTRAINT ck_member_auth_type
		CHECK (auth_type IN ('LOCAL', 'SOCIAL')),

	CONSTRAINT ck_member_provider
		CHECK (provider IN ('KAKAO', 'NAVER', 'GOOGLE') OR provider IS NULL),

	CONSTRAINT ck_member_role
		CHECK (role IN ('ADMIN', 'USER')),

	CONSTRAINT ck_member_grade
		CHECK (grade IN ('SEED', 'SPROUT', 'TREE', 'FOREST')),

	CONSTRAINT ck_member_auth_fields
		CHECK (
			(
				auth_type = 'LOCAL'
					AND username IS NOT NULL
					AND password IS NOT NULL
					AND provider IS NULL
					AND provider_id IS NULL
				)
				OR
			(
				auth_type = 'SOCIAL'
					AND username IS NULL
					AND password IS NULL
					AND provider IS NOT NULL
					AND provider_id IS NOT NULL
				)
			),

	CONSTRAINT uq_member_username UNIQUE (username),
	CONSTRAINT uq_member_phone UNIQUE (phone),
	CONSTRAINT uq_member_nickname UNIQUE (nickname),
	CONSTRAINT uq_member_provider UNIQUE (provider, provider_id)
);